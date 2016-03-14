package ru.gkpromtech.exhibition.utils;


import android.content.Context;
import android.content.res.Configuration;

public class DeviceUtils {

    public static boolean isAbleToCall(Context context)
    {
        return context.getPackageManager().hasSystemFeature("android.hardware.telephony");
    }

    public static boolean isLarge(Context context)
    {
        return (0xf & context.getResources().getConfiguration().screenLayout) == 3;
    }

    public static boolean isTablet(Context context)
    {
        return isXLarge(context) || isLarge(context);
    }

    public static boolean isXLarge(Context context)
    {
        return (0xf & context.getResources().getConfiguration().screenLayout) == 4;
    }

    public static boolean isLandscapeTablet(Context context) {
        return isTablet(context) && context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }
}
