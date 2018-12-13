package com.inevitable.pgpkeyboard

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.INotificationSideChannel
import android.view.View;

class FloatingView(context: Context) : View(context) {
    var paint:Paint= Paint()
    var high:Int=150
    var wide:Int=150

    override protected fun onMeasure(widthMeasureSpec:Int , heightMeasureSpec:Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(high,wide);
    }


    override protected fun onDraw(canvas:Canvas ) {
        super.onDraw(canvas);
        //画大圆
        paint.style = Paint.Style.FILL;
        paint.isAntiAlias = true;
        paint.color = resources.getColor(R.color.state_one);
        canvas.drawCircle((width/2).toFloat(),(width/2).toFloat(),(width/2).toFloat(),paint);
        //画小圆圈
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        canvas.drawCircle((width/2).toFloat(),(width/2).toFloat(), (width*1.0/4).toFloat(),paint);

    }
}


