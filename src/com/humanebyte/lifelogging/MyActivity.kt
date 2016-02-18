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
    internal lateinit var button_bypass_keyevent: Button
    internal lateinit var button_eat_keyevent: Button

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
        button_bypass_keyevent = findViewById(R.id.bypass_keyevent) as Button
        button_bypass_keyevent.setOnClickListener(this)
        button_eat_keyevent = findViewById(R.id.eat_keyevent) as Button
        button_eat_keyevent.setOnClickListener(this)
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
                val i = Intent("com.humanebyte.lifelogging.force_speaker").putExtra("force_speaker", true)
                this.sendBroadcast(i)
            }
            R.id.cancel_force_use -> {
                val i = Intent("com.humanebyte.lifelogging.force_speaker").putExtra("force_speaker", false)
                this.sendBroadcast(i)
            }
            R.id.bypass_keyevent -> {
                val i = Intent("com.humanebyte.lifelogging.eat_keyevent").putExtra("eat", false)
                this.sendBroadcast(i)
            }
            R.id.eat_keyevent -> {
                val i = Intent("com.humanebyte.lifelogging.eat_keyevent").putExtra("eat", true)
                this.sendBroadcast(i)
            }
        }
    }
}