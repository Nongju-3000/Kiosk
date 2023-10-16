package com.wook.web.lighten.nanumAED.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.wook.web.lighten.nanumAED.R;


public class IntroActivity extends AppCompatActivity {

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        float screenWidth = dm.widthPixels / dm.density;
        float screenHeight = dm.heightPixels / dm.density;
        Log.e("Test", "width = "+screenWidth+", screenHeight = "+screenHeight);
        handler = new Handler();
        handler.postDelayed(rIntent, 1000);
    }

    Runnable rIntent = () -> {
        Intent main = new Intent(IntroActivity.this, CPRActivity.class); //ModeSelection
        startActivity(main);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(rIntent);
    }
}
