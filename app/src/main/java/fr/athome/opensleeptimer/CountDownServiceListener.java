package fr.athome.opensleeptimer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.function.Consumer;

public class CountDownServiceListener {
    private Activity activity;
    private Consumer<Integer> onTick;
    private Runnable onFinished;
    private Runnable onCanceled;
    private Receiver receiver;

    public CountDownServiceListener(Activity activity, Consumer<Integer> onTick, Runnable onFinished, Runnable onCanceled) {
        this.activity = activity;
        this.onTick = onTick;
        this.onFinished = onFinished;
        this.onCanceled = onCanceled;
        this.receiver = new Receiver();
    }

    public void register() {
        this.activity.registerReceiver(receiver, new IntentFilter(CountDownService.ACTION_TICK));
        this.activity.registerReceiver(receiver, new IntentFilter(CountDownService.ACTION_FINISHED));
        this.activity.registerReceiver(receiver, new IntentFilter(CountDownService.ACTION_CANCELED));
    }

    public void unregister() {
        activity.unregisterReceiver(receiver);
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            Log.i("INFO", String.format("Action received %s", intent.getAction()));
            switch (intent.getAction()) {
                case CountDownService.ACTION_TICK:
                    int remainingSeconds = intent.getIntExtra(CountDownService.ACTION_TICK_SECONDS_REMAINING, 0);
                    onTick.accept(remainingSeconds);
                    break;
                case CountDownService.ACTION_FINISHED:
                    onFinished.run();
                    break;
                case CountDownService.ACTION_CANCELED:
                    onCanceled.run();
                    break;
            }
        }
    }
}
