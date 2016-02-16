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

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        service_status = findViewById(R.id.accessibility_service_status) as TextView
        setting = findViewById(R.id.setting) as Button
        setting.setOnClickListener(this)
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
        }
    }
}