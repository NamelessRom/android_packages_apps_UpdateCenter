package com.fima.cardsui;

import android.content.Context;
import android.util.DisplayMetrics;

public class Utils {

    /**
     * This method converts device specific pixels to device independent pixels.
     *
     * @param px  A value in px (pixels) unit. Which we need to convert into db
     * @param ctx Context to get resources and device specific display metrics
     * @return A float value to represent db equivalent to px value
     */
    public float convertPixelsToDp(Context ctx, float px) {
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    public static int convertDpToPixelInt(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / 160f));
    }

    public static float convertDpToPixel(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (dp * (metrics.densityDpi / 160f));
    }

}
