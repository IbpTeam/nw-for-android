// Copyright 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.content_shell;import org.chromium.content_shell_apk.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.os.Environment;

import org.chromium.base.CalledByNative;
import org.chromium.base.JNINamespace;
import org.chromium.base.ThreadUtils;
import org.chromium.content.browser.ActivityContentVideoViewClient;
import org.chromium.content.browser.ContentVideoViewClient;
import org.chromium.content.browser.ContentViewClient;
import org.chromium.content.browser.ContentViewCore;
import org.chromium.content.browser.ContentViewRenderView;
import org.chromium.ui.base.WindowAndroid;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;


/**
 * Container and generator of ShellViews.
 */
@JNINamespace("content")
public class ShellManager extends FrameLayout {

    public static final String DEFAULT_SHELL_URL = "file:///android_asset/index.html";
    private static boolean sStartup = true;
    private WindowAndroid mWindow;
    private Shell mActiveShell;

    final Context mContext;
    public static String ASSETS_NODE_MODULES = "node_modules";

    private String mStartupUrl = DEFAULT_SHELL_URL;

    // The target for all content rendering.
    private ContentViewRenderView mContentViewRenderView;
    private ContentViewClient mContentViewClient;

    public interface ResultOfCopyNodeModuleToSdcard {
        void onSuccess();
        void onFailure();
    }

    private String NODE_MODULES_PATH;
    private String NODE_MODULES_PATH_SD;
    private void initNodeModulePath(final Context context) {
        try {
            NODE_MODULES_PATH = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).dataDir + "/node_modules";
            NODE_MODULES_PATH_SD =  Environment.getExternalStorageDirectory().getPath() + "/content_shell/node_modules";
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean mCopySuccess = false;
    private void copyNodeModuleToSdcard(final Context ctx, final ResultOfCopyNodeModuleToSdcard callback) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                if (copyFilesFromassets(ctx, ASSETS_NODE_MODULES, NODE_MODULES_PATH)) {
                    callback.onSuccess();
                } else {
                    callback.onFailure();
                }
            }
        })).start();
    }
    private boolean copyFilesFromassets(Context context,String oldPath,String newPath) {
            try {
            String fileNames[] = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {
                File file = new File(newPath);
                file.mkdirs();
                for (String fileName : fileNames) {
                   copyFilesFromassets(context,oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("siyt", "Copy failed: e = " + e);
            //MainAsctivity.handler.sendEmptyMessage(COPY_FALSE);
        }
        return true;
    }

    public String nodeModePath() { return NODE_MODULES_PATH; }

    /**
     * Constructor for inflating via XML.
     */
    public ShellManager(final Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initNodeModulePath(mContext);
        nativeInit(this);
        mContentViewClient = new ContentViewClient() {
            @Override
            public ContentVideoViewClient getContentVideoViewClient() {
                return new ActivityContentVideoViewClient((Activity) context) {
                    @Override
                    public void enterFullscreenVideo(View view) {
                        super.enterFullscreenVideo(view);
                        setOverlayVideoMode(true);
                    }

                    @Override
                    public void exitFullscreenVideo() {
                        super.exitFullscreenVideo();
                        setOverlayVideoMode(false);
                    }
                };
            }
        };
    }

    /**
     * @param window The window used to generate all shells.
     */
    public void setWindow(WindowAndroid window) {
        assert window != null;
        mWindow = window;
        mContentViewRenderView = new ContentViewRenderView(getContext()) {
            @Override
            protected void onReadyToRender() {
                if (sStartup) {
                    mActiveShell.loadUrl(mStartupUrl);
                    sStartup = false;
                }
            }

            @Override
            protected void onWindowVisibilityChanged(int visibility) {
                if (visibility == View.GONE && mActiveShell != null) {
                    ContentViewCore contentViewCore = mActiveShell.getContentViewCore();
                    if (contentViewCore != null) contentViewCore.onHide();
                }
                super.onWindowVisibilityChanged(visibility);
            }
        };
        mContentViewRenderView.onNativeLibraryLoaded(window);
    }

    /**
     * @return The window used to generate all shells.
     */
    public WindowAndroid getWindow() {
        return mWindow;
    }

    /**
     * Sets the startup URL for new shell windows.
     */
    public void setStartupUrl(String url) {
        mStartupUrl = url;
    }

    /**
     * @return The currently visible shell view or null if one is not showing.
     */
    public Shell getActiveShell() {
        return mActiveShell;
    }

    /**
     * Creates a new shell pointing to the specified URL.
     * @param url The URL the shell should load upon creation.
     */
    public void launchShell(String url) {
        // create a thread to get node_module and copy it to sdcard.
        if (!mCopySuccess) {
            copyNodeModuleToSdcard(mContext,
                new ResultOfCopyNodeModuleToSdcard() {
                    @Override
                    public void onSuccess() {
                        mCopySuccess = true;
                    }

                    @Override
                    public void onFailure() {
                        mCopySuccess = true;
                    }
                }
            );
        }

        ThreadUtils.assertOnUiThread();
        Shell previousShell = mActiveShell;
        nativeLaunchShell(url);
        if (previousShell != null) previousShell.close();
        if (mActiveShell != null) mActiveShell.getContentViewCore().getContentSettings().SetNodeModuesPath(NODE_MODULES_PATH+":"+NODE_MODULES_PATH_SD);
    }

    /**
     * Enter or leave overlay video mode.
     * @param enabled Whether overlay mode is enabled.
     */
    public void setOverlayVideoMode(boolean enabled) {
        if (mContentViewRenderView == null) return;
        mContentViewRenderView.setOverlayVideoMode(enabled);
    }

    @SuppressWarnings("unused")
    @CalledByNative
    private Object createShell(long nativeShellPtr) {
        assert mContentViewRenderView != null;
        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Shell shellView = (Shell) inflater.inflate(R.layout.shell_view, null);
        shellView.initialize(nativeShellPtr, mWindow, mContentViewClient);

        // TODO(tedchoc): Allow switching back to these inactive shells.
        if (mActiveShell != null) removeShell(mActiveShell);

        showShell(shellView);
        return shellView;
    }

    private void showShell(Shell shellView) {
        shellView.setContentViewRenderView(mContentViewRenderView);
        addView(shellView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        mActiveShell = shellView;
        ContentViewCore contentViewCore = mActiveShell.getContentViewCore();
        if (contentViewCore != null) {
            mContentViewRenderView.setCurrentContentViewCore(contentViewCore);
            contentViewCore.onShow();
        }
    }

    @CalledByNative
    private void removeShell(Shell shellView) {
        if (shellView == mActiveShell) mActiveShell = null;
        if (shellView.getParent() == null) return;
        ContentViewCore contentViewCore = shellView.getContentViewCore();
        if (contentViewCore != null) contentViewCore.onHide();
        shellView.setContentViewRenderView(null);
        removeView(shellView);
    }

    private static native void nativeInit(Object shellManagerInstance);
    private static native void nativeLaunchShell(String url);
}
