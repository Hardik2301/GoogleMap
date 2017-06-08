package com.example.imac.samplemap.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by imac on 3/9/17.
 */

public class Screensize {

    private static int width;

    public static int getCardItemWidth(Context mContext){
        DisplayMetrics metrics=mContext.getResources().getDisplayMetrics();
        int tmpwidth=metrics.widthPixels;
        width=(tmpwidth*48)/100;
        return width;
    }

    public static int getCardItemHeight(){
        int height=(width*80)/100;
        return height;
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    public static int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }
}
