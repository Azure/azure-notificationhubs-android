package com.example.notification_hubs_sample_app_java.cookbook;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Locale;

/**
 * Examples that are ready for copy/paste usage of how to get particular data about the device this
 * application is running on.
 */
public class DevicePropertyProvider {
    /**
     * Returns a tag that is normalized across platforms, to allow targeting of any device in a
     * particular country.
     *
     * @return A string that can be used to target devices in a country.
     */
    public static String getCountryTag() {
        return "Country_" + Locale.getDefault().getCountry();
    }

    /**
     * Returns a tag that is normalized across platforms, to allow targeting of any device where the
     * user locale is set to a particular language.
     * @return A string that can be used to target device using a language.
     */
    public static String getLanguageTag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return "Language_" + Locale.getDefault().toLanguageTag();
        }

        // Do you need this value to be populated in Android versions before Lollipop? Please tell us at:
        // https://github.com/Azure/azure-notificationhubs-android/issues
        throw new UnsupportedOperationException("Language tag logic was added to the Android standard library in API Level 21 (Lollipop)");
    }

    /**
     * Returns a tag that is normalized across platforms to allow targeting of any device connected
     * to a particular mobile carrier's network.
     * @param context The Application context.
     * @return A string that can be used to target devices on a particular mobile network.
     */
    public static String getCarrierTag(Context context) {
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();

        return "MobileCarrier_" + carrierName;
    }

    /**
     * Returns a tag that allows for the targeting to all devices made by a particular company.
     * @return A string that can be used to target devices manufactured by a given company.
     */
    public static String getOemTag() {
        return "Oem_" + Build.MANUFACTURER;
    }

    /**
     * Returns a tag that is normalized across platforms to allow for the targeting of any device
     * with a specific screen-size.
     *
     * The resolution that is used will reflect the device's screen resolution minus any permanent
     * graphical elements. Further, width and height will reflect the devices' un-rotated position.
     *
     * @param context The Application context.
     * @return A string that contains the screen dimensions of the current device.
     */
    public static String getScreenSizeTag(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point resolution = new Point();
        display.getSize(resolution);

        switch (display.getRotation()) {
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                resolution.set(resolution.y, resolution.x);
                break;
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                // Intentionally Left Blank
                break;
            default:
                throw new IllegalStateException("Screen-size can only be determined when screen is rotated at a right-angle");
        }

        return "ScreenSize_" + resolution.x +
                'X' +
                resolution.y;
    }
}
