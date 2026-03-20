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
    private boolean isInitialized;

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

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float left = dpToPx(12f);
        float right = getWidth() - dpToPx(12f);
        float top = currentY - batonHeight / 2f;
        float bottom = currentY + batonHeight / 2f;
        canvas.drawRoundRect(left, top, right, bottom, batonHeight / 2f, batonHeight / 2f, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            moveTo(event.getY());
            return true;
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
