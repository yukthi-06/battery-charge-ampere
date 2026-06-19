package com.vypeensoft.chargeampmeter.helpers;

import android.os.Handler;
import android.os.Looper;

import java.util.Locale;

public class TimerManager {
    public interface TimerListener {
        void onTick(long totalSeconds, String formattedTime);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TimerListener listener;
    private long secondsElapsed = 0;
    private boolean isRunning = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                secondsElapsed++;
                if (listener != null) {
                    listener.onTick(secondsElapsed, formatTime(secondsElapsed));
                }
                handler.postDelayed(this, 1000);
            }
        }
    };

    public TimerManager(TimerListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            handler.postDelayed(timerRunnable, 1000);
        }
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
    }

    public void reset() {
        stop();
        secondsElapsed = 0;
        if (listener != null) {
            listener.onTick(0, formatTime(0));
        }
    }

    public long getSecondsElapsed() {
        return secondsElapsed;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}
