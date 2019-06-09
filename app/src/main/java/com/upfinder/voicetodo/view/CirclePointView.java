package com.upfinder.voicetodo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author: ywj
 * @date: 2019/1/9 15:34
 */
public class CirclePointView extends View {

    private int color = 0x000000;
    private int height;
    private int width;
    private Paint paint;

    public CirclePointView(Context context) {
        this(context, null);
    }


    public CirclePointView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CirclePointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        paint = new Paint();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(color);
        paint.setAntiAlias(true);
        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        int radiusY=height / 2-paddingTop/2;
        int radiusX=width / 2-paddingLeft/2;
        canvas.drawCircle(width / 2, height / 2, radiusX<radiusY?radiusX:radiusY, paint);
    }

    public CirclePointView setColor(int color) {
        this.color = color;
        invalidate();
        return this;
    }


    @Override
    public void setBackground(Drawable background) {
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
            invalidate();
        } else {
            super.setBackground(background);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        this.color = color;
        invalidate();
    }
}
