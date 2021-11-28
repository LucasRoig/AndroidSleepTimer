package fr.athome.opensleeptimer;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class CountDownService extends Service {
    public static final String TIMEOUT_IN_SEC = "timeOut";
    public static final String ACTION_TICK = "ACTION_TICK";
    public static final String ACTION_TICK_SECONDS_REMAINING = "ACTION_TICK_SECONDS_REMAINING";
    public static final String ACTION_FINISHED = "ACTION_FINISHED";
    public static final String ACTION_CANCELED = "ACTION_CANCELED";
    public static final String NOTIF_ACTION_ADD_TIME = "NOTIF_ACTION_ADD_TIME";

    private CountDownTimer timer;
    private NotificationManagerCompat  notificationManagerCompat;
    private final NotificationActionReceiver notificationActionReceiver = new NotificationActionReceiver();
    private int remainingTimeInSec = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.notificationManagerCompat = NotificationManagerCompat.from(this);
        int duration = intent.getIntExtra(TIMEOUT_IN_SEC, 10 * 60);
        this.showNotification(duration);
        this.timer = createTimer(duration);
        this.timer.start();
        registerReceiver(notificationActionReceiver, new IntentFilter(NOTIF_ACTION_ADD_TIME));
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteNotification();
        sendBroadcast(new Intent(ACTION_CANCELED));
        if (timer != null) {
            timer.cancel();
        }
        unregisterReceiver(notificationActionReceiver);
    }

    private CountDownTimer createTimer(int durationInSec) {
        return new CountDownTimer(durationInSec * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeInSec = (int) (millisUntilFinished / 1000);
                showNotification(remainingTimeInSec);
                Intent intent = new Intent(ACTION_TICK);
                intent.putExtra(ACTION_TICK_SECONDS_REMAINING, remainingTimeInSec);
                sendBroadcast(intent);
                Log.i("INFO", String.format("Time until finish %d", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                deleteNotification();
                sendBroadcast(new Intent(ACTION_FINISHED));
                Log.i("INFO", "CountDownFinished");
            }
        };
    }

    private void addTime() {
        Log.i("INFO", "adding time ---------------");
        this.timer.cancel();
        this.remainingTimeInSec += 15 * 60;
        this.timer = this.createTimer(this.remainingTimeInSec);
        this.timer.start();
    }

    private void showNotification(int durationInSec) {
        Log.i("INFO", "create notification");

        Notification notification = buildNotification(durationInSec);
//        notificationManagerCompat.notify(0, notification);
        startForeground(1, notification);
    }

    private Notification buildNotification(int durationInSec) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent addTime = new Intent(NOTIF_ACTION_ADD_TIME);
        PendingIntent addTimePendingIntent = PendingIntent.getBroadcast(this, 0, addTime, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Sleep Timer")
                .setContentText(String.format("Remaining time: %d minutes", durationInSec / 60))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(R.drawable.ic_launcher_background, "Add 15 min", addTimePendingIntent);
        return builder.build();
    }

    private void deleteNotification() {
        notificationManagerCompat.cancel(0);
    }

    private class NotificationActionReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            addTime();
        }
    }
}
