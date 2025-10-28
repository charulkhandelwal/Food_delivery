package com.example.food_delivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.R;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(this::checkNextScreen, 2000);
    }

    private void checkNextScreen() {
        Log.e("tannu", "bhbvghjdfg");
        String token = DocumentPrefs.getToken(this);

        if (token == null || token.isEmpty()) {

            startActivity(new Intent(this, Sign_up.class));
        } else {

            startActivity(new Intent(this, MainActivity.class));
        }

        finish();
    }
}
