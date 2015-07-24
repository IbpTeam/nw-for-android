// Copyright 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.ui.gfx;import org.chromium.content_shell_apk.R;

import android.graphics.Bitmap;

import org.chromium.base.CalledByNative;
import org.chromium.base.JNINamespace;

/**
 * Helper class to decode and sample down bitmap resources.
 */
@JNINamespace("gfx")
public class BitmapHelper {
    @CalledByNative
    private static Bitmap createBitmap(int width,
                                      int height,
                                      int bitmapFormatValue) {
        Bitmap.Config bitmapConfig = getBitmapConfigForFormat(bitmapFormatValue);
        return Bitmap.createBitmap(width, height, bitmapConfig);
    }

    /**
     * Provides a matching integer constant for the Bitmap.Config value passed.
     *
     * @param bitmapConfig The Bitmap Configuration value.
     * @return Matching integer constant for the Bitmap.Config value passed.
     */
    @CalledByNative
    private static int getBitmapFormatForConfig(Bitmap.Config bitmapConfig) {
        switch (bitmapConfig) {
            case ALPHA_8:
                return BitmapFormat.ALPHA_8;
            case ARGB_4444:
                return BitmapFormat.ARGB_4444;
            case ARGB_8888:
                return BitmapFormat.ARGB_8888;
            case RGB_565:
                return BitmapFormat.RGB_565;
            default:
                return BitmapFormat.NO_CONFIG;
        }
    }

     /**
     * Provides a matching Bitmap.Config for the enum config value passed.
     *
     * @param bitmapFormatValue The Bitmap Configuration enum value.
     * @return Matching Bitmap.Config  for the enum value passed.
     */
    private static Bitmap.Config getBitmapConfigForFormat(int bitmapFormatValue) {
        switch (bitmapFormatValue) {
            case BitmapFormat.ALPHA_8:
                return Bitmap.Config.ALPHA_8;
            case BitmapFormat.ARGB_4444:
                return Bitmap.Config.ARGB_4444;
            case BitmapFormat.RGB_565:
                return Bitmap.Config.RGB_565;
            case BitmapFormat.ARGB_8888:
            default:
                return Bitmap.Config.ARGB_8888;
        }
    }

}
