package com.humanebyte.lifelogging

/**
 * Created by edenyoon on 2016. 2. 16..
 */

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

abstract class AccessibilityEventCaptureService : AccessibilityService() {

    private var mReceiver: BroadcastReceiver? = null
    private var forceSpeakIntentReceiver: BroadcastReceiver? = null
    private var musicIntentReceiver: BroadcastReceiver? = null

    private var isEatKeyEvent: Boolean = true

    private val FOR_MEDIA = 1
    private val FORCE_NONE = 0
    private val FORCE_SPEAKER = 1

    private val setForceUse = Class.forName("android.media.AudioSystem").getMethod("setForceUse", Integer.TYPE, Integer.TYPE)

    private var cameraManager: CameraManager? = null

    private val LONG_PRESS_PERIOD: Long = 400
    private val BUTTON_PRESS_INTERVAL: Long = 200

    private var timeKeyDown: Long = 0
    private var timeKeyUp: Long = 0
    private var clickSequence: String = ""

    private val eventKeyUp = 1001
    private val handlerKeyUp = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                eventKeyUp -> {
                    handleClickSequence(clickSequence)
                    clickSequence = ""
                }
            }
        }

        private fun handleClickSequence(seq: String) {
            if (seq ==  "SS") {
                Log.d(TAG, "~~~~ Camera Torch Mode On/Off. Will ${!torchMode}")
                if (cameraManager != null) {
                    cameraManager!!.setTorchMode(cameraManager!!.cameraIdList[0], !torchMode)
                } else {
                    torchMode = false
                }
            }
        }
    }
    private var messageKeyUp: Message? = null

    private var torchMode: Boolean = false
    private val callbackTorch = object: CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String?, enabled: Boolean) {
            if (cameraManager != null && cameraManager!!.cameraIdList[0] == cameraId)
                torchMode = enabled
            super.onTorchModeChanged(cameraId, enabled)
        }

        override fun onTorchModeUnavailable(cameraId: String?) {
            torchMode = false
            super.onTorchModeUnavailable(cameraId)
        }
    }

    public override fun onKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        //Log.d(TAG, "action: ${getActionName(action)}, keyCode: ${KeyEvent.keyCodeToString(keyCode)}")
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            if (action === KeyEvent.ACTION_DOWN) {
                handlerKeyUp.removeMessages(eventKeyUp)
                timeKeyDown = System.currentTimeMillis()
            }
            else if (action == KeyEvent.ACTION_UP) {
                timeKeyUp = System.currentTimeMillis()

                if (timeKeyUp - timeKeyDown < LONG_PRESS_PERIOD)
                    clickSequence += "S"
                else
                    clickSequence += "L"

                messageKeyUp = handlerKeyUp.obtainMessage(eventKeyUp)
                handlerKeyUp.sendMessageDelayed(messageKeyUp, BUTTON_PRESS_INTERVAL)

                //notify()
            }

            if (isEatKeyEvent)
                return true
        }

        return super.onKeyEvent(event)
    }

    private fun getActionName(type: Int): String {
        when (type) {
            KeyEvent.ACTION_DOWN -> return "KeyEvent.ACTION_DOWN"
            KeyEvent.ACTION_UP -> return "KeyEvent.ACTION_UP"
            KeyEvent.ACTION_MULTIPLE -> return "KeyEvent.ACTION_MULTIPLE"
        }
        return "KeyEvent.Unknown"
    }

    private fun notify() {
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, AccessibilityEventCaptureService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = Notification.Builder(this)

        // 작은 아이콘 이미지.
        builder.setSmallIcon(R.drawable.ic_launcher)

        // 알림이 출력될 때 상단에 나오는 문구.
        builder.setTicker("미리보기 입니다.")

        // 알림 출력 시간.
        builder.setWhen(System.currentTimeMillis())

        // 알림 제목.
        builder.setContentTitle("내용보다 조금 큰 제목!")

        // 알림 내용.
        builder.setContentText("제목 하단에 출력될 내용!")

        // 알림시 사운드, 진동, 불빛을 설정 가능.
        builder.setDefaults(Notification.DEFAULT_LIGHTS)

        // 알림 터치시 반응.
        builder.setContentIntent(pendingIntent)

        // 알림 터치시 반응 후 알림 삭제 여부.
        builder.setAutoCancel(true)

        // 우선순위.
        builder.setPriority(Notification.PRIORITY_MAX)

        // 고유ID로 알림을 생성.
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(123456, builder.build())
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventText = getTypeName(event.eventType) + "====" + event.contentDescription
        Log.e("------------------------", "-------------------------")
        Log.d("PackageName", event.packageName.toString())
        Log.d("EventName", eventText)
        traverseNode(rootInActiveWindow)
        Log.e("------------------------", "-------------------------")

        if (hasMessage(event) == false) {
            return
        }

        val eventType = event.eventType
        val sourcePackageName = event.packageName as String
        val messages = event.text
        val message = messages[0]

        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {

            val sendingIntent = Intent(ACTION_CAPTURE_NOTIFICATION)

            val parcelable = event.parcelableData
            if (parcelable is Notification) {
                sendingIntent.putExtra(EXTRA_NOTIFICATION_TYPE, EXTRA_TYPE_NOTIFICATION)
            } else {
                sendingIntent.putExtra(EXTRA_NOTIFICATION_TYPE, EXTRA_TYPE_OTHERS)
            }
            sendingIntent.putExtra(EXTRA_PACKAGE_NAME, sourcePackageName)
            sendingIntent.putExtra(EXTRA_MESSAGE, message)
            applicationContext.sendBroadcast(sendingIntent)
        }
    }

    protected fun setTag(tag: String) {
        AccessibilityEventCaptureService.TAG = tag
    }

    override fun onServiceConnected() {
        Log.e("---------ServiceConnected--------------",
                "------------ServiceConnected------------")
        val info = AccessibilityServiceInfo()
        info.packageNames = arrayOf("com.android.mms")
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.notificationTimeout = 100
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        serviceInfo = info

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val isEat:Boolean = intent.getBooleanExtra("eat", true)
                isEatKeyEvent = isEat
            }
        }
        this.registerReceiver(mReceiver, IntentFilter("com.humanebyte.lifelogging.eat_keyevent"))

        forceSpeakIntentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val force_speaker:Boolean = intent.getBooleanExtra("force_speaker", true)
                if (force_speaker) {
                    setForceUse.invoke(null, FOR_MEDIA, FORCE_SPEAKER)
                } else {
                    setForceUse.invoke(null, FOR_MEDIA, FORCE_NONE)
                }
            }
        }
        this.registerReceiver(forceSpeakIntentReceiver, IntentFilter("com.humanebyte.lifelogging.force_speaker"))

        musicIntentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                    val state = intent.getIntExtra("state", -1)
                    if (state == 1) {
                        displayHeadphoneAlert()
                    } else if (state == 0) {
                        setForceUse.invoke(null, FOR_MEDIA, FORCE_NONE)
                    }
                }
            }
        }
        registerReceiver(musicIntentReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager?.registerTorchCallback(callbackTorch, null)
    }

    override fun onDestroy() {
        this.unregisterReceiver(this.mReceiver)
        this.unregisterReceiver(this.forceSpeakIntentReceiver)
        this.unregisterReceiver(this.musicIntentReceiver)
        cameraManager = null
    }

    private fun displayHeadphoneAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Is it headphone?").setCancelable(false).setPositiveButton("Yes",
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {
                        setForceUse.invoke(null, FOR_MEDIA, FORCE_NONE)
                        dialog.cancel()
                    }
                }).setNegativeButton("No",
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {
                        setForceUse.invoke(null, FOR_MEDIA, FORCE_SPEAKER)
                        dialog.cancel()
                    }
                })
        val alert = builder.create()
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show()
    }

    public override fun onGesture(gestureId: Int): Boolean {
        Log.v("THEIA", String.format("onGesture: [type] %s", gIdToString(gestureId)))
        return false
    }

    override fun onInterrupt() {
        Log.e("---------Interrupt--------------",
                "------------Interrupt------------")
    }

    private fun traverseNode(node: AccessibilityNodeInfo?) {
        if (null == node)
            return

        val count = node.childCount
        if (count > 0) {
            for (i in 0..count - 1) {
                val childNode = node.getChild(i)
                traverseNode(childNode)
            }
        } else {
            val text = node.text
            Log.d("test", "Node text = " + text)
        }

    }

    private fun getTypeName(type: Int): String {
        when (type) {
            AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START -> return "TYPE_TOUCH_EXPLORATION_GESTURE_START"
            AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END -> return "TYPE_TOUCH_EXPLORATION_GESTURE_END"
            AccessibilityEvent.TYPE_TOUCH_INTERACTION_START -> return "TYPE_TOUCH_INTERACTION_START"
            AccessibilityEvent.TYPE_TOUCH_INTERACTION_END -> return "TYPE_TOUCH_INTERACTION_END"
            AccessibilityEvent.TYPE_GESTURE_DETECTION_START -> return "TYPE_GESTURE_DETECTION_START"
            AccessibilityEvent.TYPE_GESTURE_DETECTION_END -> return "TYPE_GESTURE_DETECTION_END"
            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER -> return "TYPE_VIEW_HOVER_ENTER"
            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> return "TYPE_VIEW_HOVER_EXIT"
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> return "TYPE_VIEW_SCROLLED"
            AccessibilityEvent.TYPE_VIEW_CLICKED -> return "TYPE_VIEW_CLICKED"
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> return "TYPE_VIEW_LONG_CLICKED"
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> return "TYPE_VIEW_FOCUSED"
            AccessibilityEvent.TYPE_VIEW_SELECTED -> return "TYPE_VIEW_SELECTED"
            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED -> return "TYPE_VIEW_ACCESSIBILITY_FOCUSED"
            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED -> return "TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED"
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> return "TYPE_WINDOW_STATE_CHANGED"
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> return "TYPE_NOTIFICATION_STATE_CHANGED"
            AccessibilityEvent.TYPE_ANNOUNCEMENT -> return "TYPE_ANNOUNCEMENT"
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> return "TYPE_WINDOW_CONTENT_CHANGED"
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> return "TYPE_VIEW_TEXT_CHANGED"
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> return "TYPE_VIEW_TEXT_SELECTION_CHANGED"
            AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY -> return "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY"
        }
        return "Unknown"
    }

    private fun gIdToString(gID: Int): String {
        when (gID) {
            1 -> return "GESTURE_SWIPE_UP"
            2 -> return "GESTURE_SWIPE_DOWN"
            3 -> return "GESTURE_SWIPE_LEFT"
            4 -> return "GESTURE_SWIPE_RIGHT"
            5 -> return "GESTURE_SWIPE_LEFT_AND_RIGHT"
            6 -> return "GESTURE_SWIPE_RIGHT_AND_LEFT"
            7 -> return "GESTURE_SWIPE_UP_AND_DOWN"
            8 -> return "GESTURE_SWIPE_DOWN_AND_UP"
            9 -> return "GESTURE_SWIPE_LEFT_AND_UP"
            10 -> return "GESTURE_SWIPE_LEFT_AND_DOWN"
            11 -> return "GESTURE_SWIPE_RIGHT_AND_UP"
            12 -> return "GESTURE_SWIPE_RIGHT_AND_DOWN"
            13 -> return "GESTURE_SWIPE_UP_AND_LEFT"
            14 -> return "GESTURE_SWIPE_UP_AND_RIGHT"
            15 -> return "GESTURE_SWIPE_DOWN_AND_LEFT"
            16 -> return "GESTURE_SWIPE_DOWN_AND_RIGHT"
        }
        return "UNKNOWN"
    }

    private fun hasMessage(event: AccessibilityEvent?): Boolean {
        return event != null && event.text.size > 0
    }

    companion object {

        val ACTION_CAPTURE_NOTIFICATION = "action_capture_notification"
        val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
        val EXTRA_PACKAGE_NAME = "extra_package_name"
        val EXTRA_MESSAGE = "extra_message"

        val EXTRA_TYPE_NOTIFICATION = 0x19
        val EXTRA_TYPE_OTHERS = EXTRA_TYPE_NOTIFICATION + 1

        var TAG = AccessibilityEventCaptureService::class.java.simpleName
    }
}
