package com.humanebyte.lifelogging

/**
 * Created by edenyoon on 2016. 2. 16..
 */

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import java.util.*

object AccessibilityServiceUtil {

    fun getAllAccessibilityServices(context: Context): ArrayList<String> {
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        val allAccessibilityServices = ArrayList<String>()

        val settingValue = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)

        if (settingValue != null) {
            colonSplitter.setString(settingValue)
            while (colonSplitter.hasNext()) {
                val accessabilityService = colonSplitter.next()
                allAccessibilityServices.add(accessabilityService)
            }
        }
        return allAccessibilityServices
    }

    fun isAccessibilityServiceOn(context: Context, packageName: String, className: String): Boolean {
        val allAccessibilityServices = getAllAccessibilityServices(context)
        val concat = StringBuffer()
        concat.append(packageName)
        concat.append('/')
        concat.append(className)

        return allAccessibilityServices.contains(concat.toString())
    }
}
