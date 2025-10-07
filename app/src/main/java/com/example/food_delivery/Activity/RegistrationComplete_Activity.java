package com.example.food_delivery.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food_delivery.R;
import com.example.food_delivery.databinding.ActivityRegistrationCompleteBinding;

public class RegistrationComplete_Activity extends AppCompatActivity {
    ActivityRegistrationCompleteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityRegistrationCompleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}