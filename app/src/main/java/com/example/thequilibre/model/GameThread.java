package com.example.thequilibre.model;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
    private SurfaceHolder surfaceHolder;
    private GameView gameView;
    private boolean running;
    private Canvas stage;

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }
    public void setRunning(boolean isRunning) {
        running = isRunning;
    }
    @Override
    public void run() {
        while (running) {
            stage = null;
            try {
                stage = this.surfaceHolder.lockCanvas();
                synchronized(surfaceHolder) {
                    this.gameView.update();
                    this.gameView.draw(stage);
                }
            } catch (Exception e) {}
            finally {
                if (stage != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(stage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
