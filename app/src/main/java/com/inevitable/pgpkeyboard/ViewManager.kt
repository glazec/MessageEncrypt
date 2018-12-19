package com.inevitable.pgpkeyboard
import android.annotation.SuppressLint
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;



class ViewManager(var context: Context) {

    lateinit var floatBall: FloatingView
    lateinit var windowManager: WindowManager
    var manager: ViewManager? = null
    private var floatBallParams: WindowManager.LayoutParams? = null


    fun getInstance(context: Context): ViewManager {
        if (manager == null) {
            manager = ViewManager(context)
        }
        return manager as ViewManager
    }

    @SuppressLint("ClickableViewAccessibility")

    fun showFloatBall() {
        floatBall = FloatingView(context)
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (floatBallParams == null) {
            floatBallParams = WindowManager.LayoutParams()
            floatBallParams!!.width = floatBall.width
            floatBallParams!!.height = floatBall.height
            floatBallParams!!.gravity = Gravity.TOP or Gravity.LEFT
            floatBallParams!!.type = WindowManager.LayoutParams.TYPE_TOAST
            floatBallParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            floatBallParams!!.format = PixelFormat.RGBA_8888
        }

        windowManager.addView(floatBall, floatBallParams)

        floatBall.setOnClickListener {
//            EventBus.getDefault().post(MyAccessbilityService.BACK)
            EventBus.getDefault().post(0)
//            Toast.makeText(context, "点击了悬浮球 执行后退操作", Toast.LENGTH_SHORT).show()
        }

        floatBall.setOnLongClickListener {
//            EventBus.getDefault().post(MyAccessbilityService.HOME)
            EventBus.getDefault().post(1)
            Toast.makeText(context, "长按了悬浮球  执行返回桌面", Toast.LENGTH_SHORT).show()
            false
        }

        floatBall.setOnTouchListener(object : View.OnTouchListener {
            internal var startX: Float = 0.toFloat()
            internal var startY: Float = 0.toFloat()
            internal var tempX: Float = 0.toFloat()
            internal var tempY: Float = 0.toFloat()

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                        startY = event.rawY

                        tempX = event.rawX
                        tempY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - startX
                        val dy = event.rawY - startY
                        //计算偏移量，刷新视图
                        floatBallParams!!.x += dx.toInt()
                        floatBallParams!!.y += dy.toInt()
                        windowManager.updateViewLayout(floatBall, floatBallParams)
                        startX = event.rawX
                        startY = event.rawY
                    }
                    MotionEvent.ACTION_UP -> {
                        //判断松手时View的横坐标是靠近屏幕哪一侧，将View移动到依靠屏幕
                        var endX = event.rawX
                        val endY = event.rawY
                        if (endX < getScreenWidth() / 2) {
                            endX = 0f
                        } else {
                            endX = (getScreenWidth() - floatBall.width).toFloat()
                        }
                        floatBallParams!!.x = endX.toInt()
                        windowManager.updateViewLayout(floatBall, floatBallParams)
                        //如果初始落点与松手落点的坐标差值超过6个像素，则拦截该点击事件
                        //否则继续传递，将事件交给OnClickListener函数处理
                        if (Math.abs(endX - tempX) > 6 && Math.abs(endY - tempY) > 6) {
                            return true
                        }
                    }
                }
                return false
            }

        })
    }

    fun getScreenWidth(): Int {
        return windowManager.defaultDisplay.width
    }
}