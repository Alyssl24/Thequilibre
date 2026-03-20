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

    private boolean isBlowing = false;
    private int difficultySpeed = 18;


    public void setDifficulty(String difficulty) {

        if (difficulty.equals("Easy")) {
            difficultySpeed = 18;
        } else if (difficulty.equals("Hard")) {
            difficultySpeed = 4;
        } else {
            difficultySpeed = 10;
        }
    }
    
    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);

        setZOrderOnTop(true);
        getHolder().setFormat(android.graphics.PixelFormat.TRANSLUCENT);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(40);

        thread = new GameThread(getHolder(), this);
    }

    public void setBlowing(boolean blowing) {
        this.isBlowing = blowing;
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

        if (isBlowing) {
            temperature -= 2;
        } else {
            if (frameCounter >= difficultySpeed){
                if (temperature < maxTemperature) {
                    temperature++;
                }
                frameCounter = 0;
            }
        }

        if (temperature < 0) temperature = 0;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (canvas == null) return;

        float left = 50f;
        float top = 100f;
        float right = getWidth() - 50f;
        float bottom = top + 60f;

        float progressWidth = left + (temperature / (float) maxTemperature) * (right - left);

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