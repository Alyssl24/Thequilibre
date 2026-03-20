package com.example.thequilibre.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;

    private Paint paint;

    private int temperature = 0;
    private int maxTemperature = 100;

    private int frameCounter = 0;

    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(40);

        thread = new GameThread(getHolder(), this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;

        thread.setRunning(false);

        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}


    public void update() {

        frameCounter++;

        // ~30 secondes pour atteindre 100
        if (frameCounter >= 18) {

            if (temperature < maxTemperature) {
                temperature++;
            }

            frameCounter = 0;
        }
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (canvas == null) return;

        canvas.drawRGB(30, 30, 30);

        float left = 50;
        float top = 100;
        float right = getWidth() - 50;
        float bottom = top + 60;

        float progressWidth = left + ((temperature / (float) maxTemperature) * (right - left));


        paint.setColor(Color.DKGRAY);
        RectF background = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(background, 30, 30, paint);


        if (temperature < 50) {
            paint.setColor(Color.CYAN);
        } else if (temperature < 80) {
            paint.setColor(Color.YELLOW);
        } else {
            paint.setColor(Color.RED);
        }

        RectF progress = new RectF(left, top, progressWidth, bottom);
        canvas.drawRoundRect(progress, 30, 30, paint);

        paint.setColor(Color.WHITE);

        float tempCelsius = 20 + (temperature / (float) maxTemperature) * 40;

        String text = (int) tempCelsius + " °C";

        float textX = right - paint.measureText(text) - 20;
        float textY = top + (bottom - top) / 2 - ((paint.descent() + paint.ascent()) / 2);

        canvas.drawText(text, textX, textY, paint);
    }
}