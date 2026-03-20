package com.example.thequilibre;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BatonView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cupPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cupRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cupHandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final float MAX_BATON_ROTATION_DEGREES = 50f;
    private static final float CUP_ACCELERATION = 0.015f;
    private static final float CUP_FRICTION = 0.92f;
    private static final float CUP_MAX_SPEED = 0.035f;
    private float minY;
    private float maxY;
    private float currentY;
    private float batonHeight;
    private float batonMargin;
    private float cupWidth;
    private float cupHeight;
    private float cupInset;
    private float cupOffsetNormalized;
    private float cupVelocity;
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
        cupPaint.setColor(0xFFB5651D);
        cupRimPaint.setColor(0xFFD38A45);
        cupRimPaint.setStyle(Paint.Style.FILL);
        cupHandlePaint.setStyle(Paint.Style.STROKE);
        cupHandlePaint.setStrokeWidth(dpToPx(3f));
        cupHandlePaint.setColor(0xFF8C4A10);
        batonHeight = dpToPx(14f);
        batonMargin = dpToPx(12f);
        cupWidth = dpToPx(40f);
        cupHeight = dpToPx(32f);
        cupInset = dpToPx(8f);
        setClickable(true);
    }

    public void setCupSizeFromSquare(float squareWidth, float squareHeight) {
        if (squareWidth <= 0f || squareHeight <= 0f) {
            return;
        }
        cupWidth = Math.max(dpToPx(20f), squareWidth * (2f / 3f));
        cupHeight = Math.max(dpToPx(18f), squareHeight * (2f / 3f));
        cupInset = Math.max(dpToPx(6f), cupWidth * 0.08f);
        cupHandlePaint.setStrokeWidth(Math.max(dpToPx(2f), cupWidth * 0.06f));
        invalidate();
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
        float tilt = clamp(rotationDegrees / MAX_BATON_ROTATION_DEGREES, -1f, 1f);
        cupVelocity += tilt * CUP_ACCELERATION;
        cupVelocity = clamp(cupVelocity, -CUP_MAX_SPEED, CUP_MAX_SPEED);
        cupVelocity *= CUP_FRICTION;
        cupOffsetNormalized += cupVelocity;
        if (cupOffsetNormalized < -1f) {
            cupOffsetNormalized = -1f;
            cupVelocity = 0f;
        } else if (cupOffsetNormalized > 1f) {
            cupOffsetNormalized = 1f;
            cupVelocity = 0f;
        }
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

        float handleReach = cupWidth * 0.22f;
        float halfTravel = Math.max(0f, ((right - left) / 2f) - (cupWidth / 2f) - handleReach - cupInset);
        float cupCenterX = (getWidth() / 2f) + (cupOffsetNormalized * halfTravel);
        float cupBottomY = top - dpToPx(2f);
        float cupTopY = cupBottomY - cupHeight;
        float cupLeft = cupCenterX - (cupWidth / 2f);
        float cupRight = cupCenterX + (cupWidth / 2f);

        float topInset = cupWidth * 0.08f;
        float bottomInset = cupWidth * 0.20f;

        Path bodyPath = new Path();
        bodyPath.moveTo(cupLeft + topInset, cupTopY);
        bodyPath.lineTo(cupRight - topInset, cupTopY);
        bodyPath.lineTo(cupRight - bottomInset, cupBottomY);
        bodyPath.lineTo(cupLeft + bottomInset, cupBottomY);
        bodyPath.close();
        canvas.drawPath(bodyPath, cupPaint);

        float rimHeight = Math.max(dpToPx(3f), cupHeight * 0.08f);
        canvas.drawRoundRect(
                cupLeft + (topInset * 0.45f),
                cupTopY - (rimHeight * 0.35f),
                cupRight - (topInset * 0.45f),
                cupTopY + rimHeight,
                rimHeight,
                rimHeight,
                cupRimPaint
        );

        float handleStartX = cupRight - (topInset * 0.1f);
        float handleTopY = cupTopY + (cupHeight * 0.26f);
        float handleBottomY = cupTopY + (cupHeight * 0.74f);
        Path handlePath = new Path();
        handlePath.moveTo(handleStartX, handleTopY);
        handlePath.cubicTo(
                cupRight + handleReach,
                cupTopY + (cupHeight * 0.24f),
                cupRight + handleReach,
                cupTopY + (cupHeight * 0.76f),
                handleStartX,
                handleBottomY
        );
        canvas.drawPath(handlePath, cupHandlePaint);
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

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
