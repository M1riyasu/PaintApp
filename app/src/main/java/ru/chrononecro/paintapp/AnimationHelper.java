package ru.chrononecro.paintapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class AnimationHelper {
    private List<Bitmap> frames = new ArrayList<>();
    private int currentFrame = 0;
    private Canvas canvas;
    private Handler animationHandler = new Handler();
    private Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {
            canvas.drawBitmap(frames.get(currentFrame), 0, 0, null);
            currentFrame = (currentFrame + 1) % frames.size();
            if (currentFrame < frames.size())
                animationHandler.postDelayed(this, 100);
        }
    };
    public void addFrame(Bitmap frame) {
        frames.add(frame);
    }

    public void deleteCurrentFrame() {
        if (!frames.isEmpty() && currentFrame < frames.size()) {
            frames.remove(currentFrame);
            currentFrame = Math.max(0, currentFrame - 1);
        }
    }

    public void playAnimation(Canvas canvas) {
        this.canvas = canvas;
        animationHandler.removeCallbacks(animationRunnable);
        animationHandler.postDelayed(animationRunnable, 100);
    }
}

