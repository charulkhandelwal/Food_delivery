package com.example.food_delivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivitySplashBinding;

public class splash extends AppCompatActivity {
    ActivitySplashBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(splash.this, Sign_up.class);
            startActivity(intent);
            finish();
        }, 3000);


    }

        private void checkNextScreen() {
            String token = DocumentPrefs.getToken(this);
            String profile = DocumentPrefs.getProfile(this);
            boolean docsUploaded = DocumentPrefs.getDocsUploaded(this);

            if (token == null) {
                startActivity(new Intent(this, Sign_up.class));
            } else if (profile == null) {
                startActivity(new Intent(this, Personal_informationActivity.class));
            } else if (!docsUploaded) {
                startActivity(new Intent(this, Document_Activity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }



}