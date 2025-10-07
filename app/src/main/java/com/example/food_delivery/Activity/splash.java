package com.example.food_delivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.databinding.ActivitySplashBinding;

public class splash extends AppCompatActivity {
    ActivitySplashBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding= ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(splash.this, Sign_up.class);
            startActivity(intent);
            finish();
        }, 3000);

    }
}