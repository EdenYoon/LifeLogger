package com.humanebyte.lifelogging

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView

class MyActivity : Activity(), View.OnClickListener {
    internal lateinit var service_status: TextView
    internal lateinit var setting: Button
    internal lateinit var button_force_speaker: Button
    internal lateinit var button_cancel_force_use: Button

    internal val FOR_MEDIA = 1
    internal val FORCE_NONE = 0
    internal val FORCE_SPEAKER = 1

    internal val setForceUse = Class.forName("android.media.AudioSystem").getMethod("setForceUse", Integer.TYPE, Integer.TYPE)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        service_status = findViewById(R.id.accessibility_service_status) as TextView
        setting = findViewById(R.id.setting) as Button
        setting.setOnClickListener(this)
        button_force_speaker = findViewById(R.id.force_speaker) as Button
        button_force_speaker.setOnClickListener(this)
        button_cancel_force_use = findViewById(R.id.cancel_force_use) as Button
        button_cancel_force_use.setOnClickListener(this)
    }

    override fun onResume() {
        val isSet = AccessibilityServiceUtil.isAccessibilityServiceOn(
                applicationContext,
                "com.humanebyte.lifelogging",
                "com.humanebyte.lifelogging.MyAccessibilityService")

        if (isSet) {
            service_status.setText(R.string.accessibility_service_on)
        } else {
            service_status.setText(R.string.accessibility_service_off)
        }

        super.onResume()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.setting -> {
                val accessibilityServiceIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(accessibilityServiceIntent)
            }
            R.id.force_speaker -> {
                try {
                    setForceUse.invoke(null, FOR_MEDIA, FORCE_SPEAKER)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.cancel_force_use -> {
                try {
                    setForceUse.invoke(null, FOR_MEDIA, FORCE_NONE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}