package com.py.player.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.py.player.ui.MaxApplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

public class Util {
 
    public static void toaster(Context context, int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
    }

    public static int convertPxToDp(int px) {
        DisplayMetrics metrics = MaxApplication.getAppResources().getDisplayMetrics();
        float logicalDensity = metrics.density;
        int dp = Math.round(px / logicalDensity);
        return dp;
    }

    public static int convertDpToPx(int dp) {
        return Math.round(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                		MaxApplication.getAppResources().getDisplayMetrics())
                );
    }

    public static String readAsset(String assetName, String defaultS) {
        try {
            InputStream is = MaxApplication.getAppResources().getAssets().open(assetName);
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF8"));
            StringBuilder sb = new StringBuilder();
            String line = r.readLine();
            if(line != null) {
                sb.append(line);
                line = r.readLine();
                while(line != null) {
                    sb.append('\n');
                    sb.append(line);
                    line = r.readLine();
                }
            }
            return sb.toString();
        } catch (IOException e) {
            return defaultS;
        }
    }


    public static int getResourceFromAttribute(Context context, int attrId) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[] {attrId});
        int resId = a.getResourceId(0, 0);
        a.recycle();
        return resId;
    }

 
    public static void setAlignModeByPref(int alignMode, TextView t) {
        if(alignMode == 1)
            t.setEllipsize(TruncateAt.END);
        else if(alignMode == 2)
            t.setEllipsize(TruncateAt.START);
        else if(alignMode == 3) {
            t.setEllipsize(TruncateAt.MARQUEE);
            t.setMarqueeRepeatLimit(-1);
            t.setSelected(true);
        }
    }
}
