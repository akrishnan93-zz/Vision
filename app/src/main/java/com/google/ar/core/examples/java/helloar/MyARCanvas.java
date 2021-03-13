package com.google.ar.core.examples.java.helloar;

import android.view.View;
import android.graphics.Canvas;
import android.graphics.*;
import android.content.Context;

public class MyARCanvas extends View {

    Paint paint;
    Rect rect;

    public MyARCanvas(Context context) {
        super(context);
        paint = new Paint();
        rect = new Rect();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);

        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight() / 2, paint);

    }

}