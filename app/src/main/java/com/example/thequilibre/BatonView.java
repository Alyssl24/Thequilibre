package com.example.thequilibre;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BatonView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float minY;
    private float maxY;
    private float currentY;
    private float batonHeight;
    private float batonMargin;
    private float rotationDegrees;
    private boolean isInitialized;
    private boolean isDragging;
    private float touchStartY;
    private float batonStartY;

    public BatonView(Context context) {
        super(context);
        init();
    }

    public BatonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BatonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(0xFF2F2F2F);
        batonHeight = dpToPx(14f);
        batonMargin = dpToPx(12f);
        setClickable(true);
    }

    public void setMovementBounds(float minY, float maxY) {
        this.minY = minY;
        this.maxY = maxY;
        if (!isInitialized) {
            currentY = minY;
            isInitialized = true;
        } else {
            currentY = clamp(currentY);
        }
        invalidate();
    }

    public void moveTo(float y) {
        currentY = clamp(y);
        invalidate();
    }

    public void setBatonRotationDegrees(float rotationDegrees) {
        this.rotationDegrees = rotationDegrees;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float left = batonMargin;
        float right = getWidth() - batonMargin;
        float top = currentY - batonHeight / 2f;
        float bottom = currentY + batonHeight / 2f;

        canvas.save();
        canvas.rotate(rotationDegrees, getWidth() / 2f, currentY);
        canvas.drawRoundRect(left, top, right, bottom, batonHeight / 2f, batonHeight / 2f, paint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            isDragging = true;
            touchStartY = event.getY();
            batonStartY = currentY;
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (!isDragging) {
                return false;
            }
            float deltaY = event.getY() - touchStartY;
            moveTo(batonStartY + deltaY);
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            boolean wasDragging = isDragging;
            isDragging = false;
            return wasDragging;
        }
        return super.onTouchEvent(event);
    }

    private float clamp(float value) {
        if (maxY < minY) {
            return value;
        }
        return Math.max(minY, Math.min(maxY, value));
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
