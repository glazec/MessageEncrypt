package com.inevitable.pgpkeyboard

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import android.content.Intent
import android.util.Log
import android.util.Log.*


class MyAccessbilityService:AccessibilityService() {

        val BACK: Int = 1;
        val HOME: Int  = 2;
        val TAG:String  = "ICE";


        override fun onCreate() {
            super.onCreate();
            //使用EventBus代替广播
            EventBus.getDefault().register(this);
        }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: 点击了")
        return super.onStartCommand(intent, flags, startId)
    }

        override fun onAccessibilityEvent(event: AccessibilityEvent)
        {}

        override fun onInterrupt()
        {}

        @Subscribe
        fun onReceive(action: Int)
        {
            when(action) {
                BACK ->
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
//                break;
                HOME->
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
//                break;
            }
        }



}