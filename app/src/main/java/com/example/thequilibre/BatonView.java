package com.example.thequilibre;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BatonView extends View {

    private final Paint batonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cupPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint teaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint steamPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path steamPath = new Path();
    private final RectF cupRect = new RectF();
    private final RectF teaRect = new RectF();
    private final RectF handleRect = new RectF();
    private float minY;
    private float maxY;
    private float currentY;
    private float batonHeight;
    private float batonMargin;
    private float rotationDegrees;
    private float cupWidthPx;
    private float cupHeightPx;
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
        batonPaint.setColor(0xFF2F2F2F);
        cupPaint.setColor(0xFF3B82F6);
        cupPaint.setStyle(Paint.Style.FILL);

        teaPaint.setColor(0xFFB07A3C);
        teaPaint.setStyle(Paint.Style.FILL);

        handlePaint.setColor(0xFF3B82F6);
        handlePaint.setStyle(Paint.Style.STROKE);
        handlePaint.setStrokeWidth(dpToPx(2f));

        steamPaint.setColor(0x80FFFFFF);
        steamPaint.setStyle(Paint.Style.STROKE);
        steamPaint.setStrokeWidth(dpToPx(1.5f));
        steamPaint.setStrokeCap(Paint.Cap.ROUND);

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

    public void setCupWidthPx(float cupWidthPx) {
        this.cupWidthPx = cupWidthPx;
        invalidate();
    }

    public void setCupHeightPx(float cupHeightPx) {
        this.cupHeightPx = cupHeightPx;
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
        canvas.drawRoundRect(left, top, right, bottom, batonHeight / 2f, batonHeight / 2f, batonPaint);
        drawTeaCup(canvas, getWidth() / 2f, currentY);
        canvas.restore();
    }

    private void drawTeaCup(Canvas canvas, float centerX, float centerY) {
        float cupWidth = cupWidthPx > 0f ? cupWidthPx : dpToPx(24f);
        float cupHeight = cupHeightPx > 0f ? cupHeightPx : dpToPx(12f);
        float barTop = centerY - batonHeight / 2f;
        float cupTop = barTop - cupHeight;
        float cupRadius = dpToPx(4f);

        cupRect.set(centerX - cupWidth / 2f, cupTop, centerX + cupWidth / 2f, barTop);
        canvas.drawRoundRect(cupRect, cupRadius, cupRadius, cupPaint);

        float teaInset = dpToPx(2f);
        teaRect.set(cupRect.left + teaInset, cupRect.top + teaInset, cupRect.right - teaInset, cupRect.top + dpToPx(5f));
        canvas.drawRoundRect(teaRect, dpToPx(2f), dpToPx(2f), teaPaint);

        float handleInset = dpToPx(3f);
        handleRect.set(cupRect.right - handleInset, cupRect.top + handleInset, cupRect.right + dpToPx(8f), cupRect.bottom - handleInset);
        canvas.drawArc(handleRect, -90f, 220f, false, handlePaint);

        float steamBaseY = cupRect.top - dpToPx(2f);
        drawSteam(canvas, centerX - dpToPx(5f), steamBaseY);
        drawSteam(canvas, centerX, steamBaseY - dpToPx(1f));
        drawSteam(canvas, centerX + dpToPx(5f), steamBaseY);
    }

    private void drawSteam(Canvas canvas, float startX, float startY) {
        steamPath.reset();
        steamPath.moveTo(startX, startY);
        steamPath.cubicTo(
                startX + dpToPx(1.5f), startY - dpToPx(4f),
                startX - dpToPx(1.5f), startY - dpToPx(8f),
                startX, startY - dpToPx(12f)
        );
        canvas.drawPath(steamPath, steamPaint);
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
