package com.example.food_delivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.R;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Set splash screen layout
        setContentView(R.layout.activity_splash);

        // ✅ Delay splash for 2 seconds before moving next
        new Handler().postDelayed(this::checkNextScreen, 2000);
    }

    private void checkNextScreen() {
        String token = DocumentPrefs.getToken(this);

        if (token == null || token.isEmpty()) {
            // 🔹 User not logged in → OTP screen
            startActivity(new Intent(this, OtpActivity.class));
        } else {
            // 🔹 Already logged in → MainActivity
            startActivity(new Intent(this, MainActivity.class));
        }

        finish();
    }
}
