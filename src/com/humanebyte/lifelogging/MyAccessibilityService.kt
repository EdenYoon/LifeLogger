package com.humanebyte.lifelogging

/**
 * Created by edenyoon on 2016. 2. 16..
 */

class MyAccessibilityService : AccessibilityEventCaptureService() {
    init {
        this.setTag(MyAccessibilityService::class.java.simpleName)
    }
}
