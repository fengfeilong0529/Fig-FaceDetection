package com.fig.camerademo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class DetectView extends View {

    private Paint mPaint;
    private Paint mEyePaint;
    private List<Point> leftEyes;
    private List<Point> rightEyes;
    private List<RectF> rects;

    public DetectView(Context context) {
        this(context, null);
    }

    public DetectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.GREEN);
        float mStrokeWidth = 6;
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mEyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEyePaint.setColor(Color.RED);
        mEyePaint.setStrokeWidth(mStrokeWidth);
        mEyePaint.setDither(true);
        mEyePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            for (Point leftEye : leftEyes) {
                canvas.drawPoint(leftEye.x, leftEye.y, mEyePaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (Point rightEye : rightEyes) {
                canvas.drawPoint(rightEye.x, rightEye.y, mEyePaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (RectF rect : rects) {
                canvas.drawRect(rect, mPaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDetectEyes(List<Point> leftEyes, List<Point> rightEyes) {
        this.leftEyes = leftEyes;
        this.rightEyes = rightEyes;
        invalidate();
    }

    public void onDetectFace(List<RectF> rects) {
        this.rects = rects;
        invalidate();
    }
}
