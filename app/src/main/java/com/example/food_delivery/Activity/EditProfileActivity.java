package com.example.food_delivery.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.food_delivery.R;
import com.example.food_delivery.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private Uri imageUri;

    // Image picker launcher
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.imgProfile.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // ✅ Initialize Data Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);

        setupUI();
    }

    private void setupUI() {
        // Prefill data (Example only — you can load this from SharedPreferences or API)
        binding.etName.setText("Aman Sharma");
        binding.etMobile.setText("9999988888");
        binding.etEmail.setText("loremipsum@gmail.com");

        // Change Photo click
        binding.tvChangePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Save Changes button
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String mobile = binding.etMobile.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();

            if (name.isEmpty() || mobile.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Normally, API call would go here to save profile
            Toast.makeText(this, "Profile updated successfully ✅", Toast.LENGTH_SHORT).show();

            // Send back data to previous Activity (optional)
            Intent resultIntent = new Intent();
            resultIntent.putExtra("name", name);
            resultIntent.putExtra("mobile", mobile);
            resultIntent.putExtra("email", email);
            if (imageUri != null) {
                resultIntent.putExtra("imageUri", imageUri.toString());
            }

            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }
}
