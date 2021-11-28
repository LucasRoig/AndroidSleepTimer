package fr.athome.opensleeptimer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import fr.athome.opensleeptimer.databinding.ActivityMainBinding;

import com.devadvance.circularseekbar.CircularSeekBar;

public class MainActivity extends AppCompatActivity {
    public static final String CHANNEL_ID = "SleepTimerNotifChannel";

    private ActivityMainBinding binding;
    private AdminActions adminActions;
    private int timeLeft = 30;
    private boolean isTimerRunning = false;
    private final CountDownServiceListener countDownServiceListener = new CountDownServiceListener(
            this,
            (remainingSeconds) -> {
                timeLeft = remainingSeconds / 60;
                updateUi();
            },
            () -> {
                unregisterListener();
                isTimerRunning = false;
                timeLeft = 30;
                updateUi();
                stopTimer();
                adminActions.lockNow();
            },
            () -> {
                unregisterListener();
                isTimerRunning = false;
                timeLeft = 30;
                updateUi();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        adminActions = new AdminActions((DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE),
                new ComponentName(this, CustomDeviceAdminReceiver.class),
                this);


       adminActions.checkAdminRights();

        binding.startButton.setOnClickListener(v -> {
            if (this.isTimerRunning) {
                stopTimer();
            } else {
                startTimer();
            }
        });

        binding.timeSeekBar.setProgress(this.timeLeft);
        binding.timeSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                timeLeft = progress;
                updateUi();
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }
        });
        updateUi();
}

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void unregisterListener() {
        this.countDownServiceListener.unregister();
    }

    private void startTimer() {
        Intent i = new Intent(this, CountDownService.class);
        i.putExtra(CountDownService.TIMEOUT_IN_SEC, this.timeLeft * 60);
        startForegroundService(i);
        this.isTimerRunning = true;
        this.countDownServiceListener.register();
        this.updateUi();
    }

    private void stopTimer() {
        Intent i = new Intent(this, CountDownService.class);
        stopService(i);
    }

    private void updateUi() {
        binding.textMinutesLeft.setText(String.format("%d minutes", this.timeLeft));
        this.binding.timeSeekBar.setProgress(Math.min(this.timeLeft, 120));
        if (this.isTimerRunning) {
            this.binding.startButton.setText("Cancel");
            this.binding.timeSeekBar.setIsTouchEnabled(false);
        } else {
            this.binding.startButton.setText("Start");
            this.binding.timeSeekBar.setIsTouchEnabled(true);
        }
    }
}