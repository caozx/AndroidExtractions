package com.caozx.extraction.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.caozx.extraction.R;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.drawable.splash);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 1000 * 3);
    }


}
