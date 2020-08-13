package com.example.notification_hubs_sample_app_kotlin.cookbook

/**
 * Examples that are ready for copy/paste usage of how to get particular data about the device this
 * application is running on.
 */
object DevicePropertyProvider {
    /**
     * Returns a tag that is normalized across platforms, to allow targeting of any device in a
     * particular country.
     *
     * @return A string that can be used to target devices in a country.
     */
    val countryTag: String
        get() = "Country_" + java.util.Locale.getDefault().getCountry()

    /**
     * Returns a tag that is normalized across platforms, to allow targeting of any device where the
     * user locale is set to a particular language.
     * @return A string that can be used to target device using a language.
     */
    val languageTag: String
        get() {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                return "Language_" + java.util.Locale.getDefault().toLanguageTag()
            }
            throw java.lang.UnsupportedOperationException("Language tag logic was added to the Android standard library in API Level 21 (Lollipop)")
        }

    /**
     * Returns a tag that is normalized across platforms to allow targeting of any device connected
     * to a particular mobile carrier's network.
     * @param context The Application context.
     * @return A string that can be used to target devices on a particular mobile network.
     */
    fun getCarrierTag(context: android.content.Context): String {
        val manager: android.telephony.TelephonyManager = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
        val carrierName: String = manager.getNetworkOperatorName()
        return "MobileCarrier_$carrierName"
    }

    /**
     * Returns a tag that allows for the targeting to all devices made by a particular company.
     * @param context The Application context.
     * @return A string that can be used to target devices manufactured by a given company.
     */
    fun getOemTag(context: android.content.Context?): String {
        return "Oem_" + android.os.Build.MANUFACTURER
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
    fun getScreenSizeTag(context: android.content.Context): String {
        val wm: android.view.WindowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager
        val disp: android.view.Display = wm.getDefaultDisplay()
        val resolution: android.graphics.Point = android.graphics.Point()
        disp.getSize(resolution)
        when (disp.getRotation()) {
            android.view.Surface.ROTATION_90, android.view.Surface.ROTATION_270 -> resolution.set(resolution.y, resolution.x)
            android.view.Surface.ROTATION_0, android.view.Surface.ROTATION_180 -> {
            }
            else -> throw java.lang.IllegalStateException("Screen-size can only be determined when screen is rotated at a right-angle")
        }
        val builder: java.lang.StringBuilder = java.lang.StringBuilder("ScreenSize_")
        builder.append(resolution.x)
        builder.append('X')
        builder.append(resolution.y)
        return builder.toString()
    }
}