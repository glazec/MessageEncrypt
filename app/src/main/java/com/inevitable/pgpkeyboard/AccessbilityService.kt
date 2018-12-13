package com.inevitable.pgpkeyboard

import android.accessibilityservice.AccessibilityService

class AccessbilityService : AccessibilityService() {
    public static final int BACK = 1;
    public static final int HOME = 2;
    private static final String TAG = "ICE";


    override fun onCreate() {
        super.onCreate();
        //使用EventBus代替广播
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {}

    @Override
    public void onInterrupt()
    {}

    @Subscribe
    public void onReceive(Integer action)
    {
        switch(action) {
            case BACK :
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            break;
            case HOME :
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
            break;
        }
    }

}
