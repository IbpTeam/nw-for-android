// Copyright 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.content.browser;import org.chromium.content_shell_apk.R;

import org.chromium.base.CalledByNative;
import org.chromium.base.JNINamespace;
import org.chromium.base.ThreadUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Manages settings state for a ContentView. A ContentSettings instance is obtained
 * from ContentViewCore.getContentSettings().
 */
@JNINamespace("content")
public class ContentSettings {

    private static final String TAG = "ContentSettings";

	private String mNodeModulesPath;

    // The native side of this object. Ownership is retained native-side by the WebContents
    // instance that backs the associated ContentViewCore.
    private long mNativeContentSettings = 0;

    // Custom handler that queues messages to call native code on the UI thread.
    private final EventHandler mEventHandler;

    private ContentViewCore mContentViewCore;

	// Lock to protect all settings.
    private final Object mContentSettingsLock = new Object();

    // Class to handle messages to be processed on the UI thread.
    private class EventHandler {
        // Message id for running a Runnable with mAwSettingsLock held.
        private static final int RUN_RUNNABLE_BLOCKING = 0;
        // Actual UI thread handler
        private Handler mHandler;
        // Synchronization flag.
        private boolean mSynchronizationPending = false;

        EventHandler() {
        }

        void bindUiThread() {
            if (mHandler != null) return;
            mHandler = new Handler(ThreadUtils.getUiThreadLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case RUN_RUNNABLE_BLOCKING:
                            synchronized (mContentSettingsLock) {
                                if (mNativeContentSettings != 0) {
                                    ((Runnable) msg.obj).run();
                                }
                                mSynchronizationPending = false;
                                mContentSettingsLock.notifyAll();
                            }
                            break;
                    }
                }
            };
        }

        void runOnUiThreadBlockingAndLocked(Runnable r) {
            assert Thread.holdsLock(mContentSettingsLock);
            if (mHandler == null) return;
            if (ThreadUtils.runningOnUiThread()) {
                r.run();
            } else {
                assert !mSynchronizationPending;
                mSynchronizationPending = true;
                mHandler.sendMessage(Message.obtain(null, RUN_RUNNABLE_BLOCKING, r));
                try {
                    while (mSynchronizationPending) {
                        mContentSettingsLock.wait();
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted waiting a Runnable to complete", e);
                    mSynchronizationPending = false;
                }
            }
        }

        void maybePostOnUiThread(Runnable r) {
            if (mHandler != null) {
                mHandler.post(r);
            }
        }

        void updateWebkitPreferencesLocked() {
            runOnUiThreadBlockingAndLocked(new Runnable() {
                @Override
                public void run() {
                    updateWebkitPreferencesOnUiThreadLocked();
                }
            });
        }
    }

    /**
     * Package constructor to prevent clients from creating a new settings
     * instance. Must be called on the UI thread.
     */
    ContentSettings(ContentViewCore contentViewCore, long nativeContentView) {
        ThreadUtils.assertOnUiThread();
        mContentViewCore = contentViewCore;
        mNativeContentSettings = nativeInit(nativeContentView);
		mEventHandler = new EventHandler();
		mEventHandler.bindUiThread();
        assert mNativeContentSettings != 0;
    }

    /**
     * Notification from the native side that it is being destroyed.
     * @param nativeContentSettings the native instance that is going away.
     */
    @CalledByNative
    private void onNativeContentSettingsDestroyed(long nativeContentSettings) {
        assert mNativeContentSettings == nativeContentSettings;
        mNativeContentSettings = 0;
    }

    @CalledByNative
    private void populateWebPreferences(long webPrefsPtr) {
        synchronized (mContentSettingsLock) {
            assert mNativeContentSettings != 0;
            nativePopulateWebPreferencesLocked(mNativeContentSettings, webPrefsPtr);
        }
    }

	public void SetNodeModuesPath(String path) {
        synchronized (mContentSettingsLock) {
            if (mNodeModulesPath != path) {
                mNodeModulesPath = path;
                mEventHandler.updateWebkitPreferencesLocked();
            }
        }
	}

	@CalledByNative
    private void updateEverything() {
        synchronized (mContentSettingsLock) {
            updateEverythingLocked();
        }
    }

	private void updateEverythingLocked() {
        assert Thread.holdsLock(mContentSettingsLock);
        assert mContentSettingsLock != null;
        nativeUpdateEverythingLocked(mNativeContentSettings);
        //onGestureZoomSupportChanged(
        //        supportsDoubleTapZoomLocked(), supportsMultiTouchZoomLocked());
    }
		
    @CalledByNative
    private String getNodeModulesPath() {
        assert Thread.holdsLock(mContentSettingsLock);
        return mNodeModulesPath;
    }

    /**
     * Return true if JavaScript is enabled. Must be called on the UI thread.
     *
     * @return True if JavaScript is enabled.
     */
    public boolean getJavaScriptEnabled() {
        ThreadUtils.assertOnUiThread();
        return mNativeContentSettings != 0
                ? nativeGetJavaScriptEnabled(mNativeContentSettings) : false;
    }

	
    private void updateWebkitPreferencesOnUiThreadLocked() {
        assert mEventHandler.mHandler != null;
        ThreadUtils.assertOnUiThread();
        if (mNativeContentSettings != 0) {
            nativeUpdateWebkitPreferencesLocked(mNativeContentSettings);
        }
    }

    // Initialize the ContentSettings native side.
    private native long nativeInit(long contentViewPtr);
    private native boolean nativeGetJavaScriptEnabled(long nativeContentSettings);
    private native void nativeUpdateEverythingLocked(long nativeContentSettings);
    private native void nativePopulateWebPreferencesLocked(long nativeContentSettings, long webPrefsPtr);
    private native void nativeUpdateWebkitPreferencesLocked(long nativeContentSettings);
}
