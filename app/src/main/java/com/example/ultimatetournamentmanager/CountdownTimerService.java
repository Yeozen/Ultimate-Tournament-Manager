package com.example.ultimatetournamentmanager;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class CountdownTimerService extends Service {

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeRemainingMillis = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case "start":
                        // Retrieve minutes and seconds from Intent extras
                        int minutes = intent.getIntExtra("minutes", 0);
                        int seconds = intent.getIntExtra("seconds", 0);


                        startTimer(minutes, seconds);
                        break;
                    case "stop":
                        stopTimer();
                        break;
                    case "reset":
                        resetTimer();
                        break;
                }
            }
        }
        return START_STICKY;
    }

    private void startTimer(int minutes, int seconds) {
        // Use the minutes and seconds values to start the timer
        if (!isTimerRunning) {
            timeRemainingMillis = (minutes * 60 + seconds) * 1000;

            countDownTimer = new CountDownTimer(timeRemainingMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeRemainingMillis = millisUntilFinished;
                    sendUpdateBroadcast();
                }

                @Override
                public void onFinish() {
                    isTimerRunning = false;
                    sendUpdateBroadcast();
                }
            }.start();

            isTimerRunning = true;
            sendUpdateBroadcast();
        }
    }



    private void stopTimer() {
        if (isTimerRunning) {
            countDownTimer.cancel();
            isTimerRunning = false;
            sendUpdateBroadcast();
        }
    }

    private void resetTimer() {
        stopTimer();
        timeRemainingMillis = 0; // Reset to your desired time
        sendUpdateBroadcast();
    }

    private void sendUpdateBroadcast() {
        Intent intent = new Intent("timerUpdate");
        intent.putExtra("timeRemaining", timeRemainingMillis);
        intent.putExtra("isTimerRunning", isTimerRunning);
        sendBroadcast(intent);
    }
}
