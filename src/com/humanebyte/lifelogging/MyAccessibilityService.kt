package com.humanebyte.lifelogging

/**
 * Created by edenyoon on 2016. 2. 16..
 */

import com.humanebyte.lifelogging.AccessibilityEventCaptureService

class MyAccessibilityService : AccessibilityEventCaptureService() {
    init {
        this.setTag(MyAccessibilityService::class.java.simpleName)
    }
}
