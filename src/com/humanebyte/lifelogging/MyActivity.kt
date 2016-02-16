package com.humanebyte.lifelogging

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView

class MyActivity : Activity() {
    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val logTextView = findViewById(R.id.textview) as TextView
        logTextView.append("Hellow wrold!!")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("Msg", "Key Down - " + keyCode);

        if (event != null) {
            val logTextView = findViewById(R.id.textview) as TextView
            var log = Character.toString(event!!.getDisplayLabel())
            logTextView.append(log)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("Msg","Key Up - " + keyCode);
        return super.onKeyUp(keyCode, event);
    }
}
