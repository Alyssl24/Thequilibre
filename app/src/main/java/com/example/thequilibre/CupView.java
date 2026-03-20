package com.example.thequilibre;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CupView extends View {

    private static final int CUP_BASE_COLOR = 0xFFB5651D;
    private static final int CUP_RIM_BASE_COLOR = 0xFFD38A45;
    private static final int CUP_HANDLE_BASE_COLOR = 0xFF8C4A10;
    private static final int CUP_HIT_COLOR = 0xFFD15743;
    private static final int CUP_HIT_RIM_COLOR = 0xFFE17466;
    private static final int CUP_HIT_HANDLE_COLOR = 0xFFAA3A2B;

    private final Paint cupPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cupRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cupHandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Nullable
    private BatonView batonView;

    public CupView(Context context) {
        super(context);
        init();
    }

    public CupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        cupPaint.setColor(CUP_BASE_COLOR);
        cupRimPaint.setColor(CUP_RIM_BASE_COLOR);
        cupRimPaint.setStyle(Paint.Style.FILL);
        cupHandlePaint.setStyle(Paint.Style.STROKE);
        cupHandlePaint.setColor(CUP_HANDLE_BASE_COLOR);
        setClickable(false);
        setFocusable(false);
        setEnabled(false);
    }

    public void attachToBaton(@NonNull BatonView batonView) {
        this.batonView = batonView;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (batonView == null || batonView.getWidth() <= 0 || batonView.getHeight() <= 0) {
            return;
        }

        float batonMargin = batonView.getBatonMarginPx();
        float batonHeight = batonView.getBatonHeightPx();
        float currentY = batonView.getCurrentY();
        float rotationDegrees = batonView.getRotationDegrees();
        float cupWidth = batonView.getCupWidthPx();
        float cupHeight = batonView.getCupHeightPx();
        float cupInset = batonView.getCupInsetPx();
        float cupOffsetNormalized = batonView.getCupOffsetNormalized();

        float left = batonMargin;
        float right = getWidth() - batonMargin;
        float top = currentY - (batonHeight / 2f);

        float handleReach = cupWidth * 0.22f;
        float halfTravel = Math.max(0f, ((right - left) / 2f) - (cupWidth / 2f) - handleReach - cupInset);
        float cupCenterX = (getWidth() / 2f) + (cupOffsetNormalized * halfTravel);
        float cupBottomY = top - dpToPx(2f);
        float cupTopY = cupBottomY - cupHeight;
        float cupLeft = cupCenterX - (cupWidth / 2f);
        float cupRight = cupCenterX + (cupWidth / 2f);

        float topInset = cupWidth * 0.08f;
        float bottomInset = cupWidth * 0.20f;
        float rimHeight = Math.max(dpToPx(3f), cupHeight * 0.08f);
        boolean isHitFlash = batonView.isDangerFlashActive();

        cupHandlePaint.setStrokeWidth(Math.max(dpToPx(2f), cupWidth * 0.06f));
        cupPaint.setColor(isHitFlash ? CUP_HIT_COLOR : CUP_BASE_COLOR);
        cupRimPaint.setColor(isHitFlash ? CUP_HIT_RIM_COLOR : CUP_RIM_BASE_COLOR);
        cupHandlePaint.setColor(isHitFlash ? CUP_HIT_HANDLE_COLOR : CUP_HANDLE_BASE_COLOR);

        canvas.save();
        canvas.rotate(rotationDegrees, getWidth() / 2f, currentY);

        Path bodyPath = new Path();
        bodyPath.moveTo(cupLeft + topInset, cupTopY);
        bodyPath.lineTo(cupRight - topInset, cupTopY);
        bodyPath.lineTo(cupRight - bottomInset, cupBottomY);
        bodyPath.lineTo(cupLeft + bottomInset, cupBottomY);
        bodyPath.close();
        canvas.drawPath(bodyPath, cupPaint);

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

        if (isHitFlash) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        return false;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
