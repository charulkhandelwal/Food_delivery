package com.example.food_delivery.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.GetProfileResponse;
import com.example.food_delivery.R;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityEditProfileBinding;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private Uri imageUri;
    private OtpApi apiService;

    private static final String TAG = "EditProfileActivity";

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);


        binding.btnSave.setVisibility(View.GONE);


        getProfileData();
        setupUI();
    }

    private void getProfileData() {
        String token = DocumentPrefs.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No token found in SharedPreferences");
            return;
        }

        Log.d(TAG, "Token found: " + token);


        apiService = ApiClient.getClientWithToken(token).create(OtpApi.class);

        Log.d(TAG, "Calling GET /delivery-partner/profile API...");

        Call<GetProfileResponse> call = apiService.getProfile();
        call.enqueue(new Callback<GetProfileResponse>() {
            @Override
            public void onResponse(Call<GetProfileResponse> call, Response<GetProfileResponse> response) {
                Log.d(TAG, "API Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    GetProfileResponse data = response.body();
                    Log.d(TAG, "Full API Response: " + new Gson().toJson(data));

                    if (data.results != null) {
                        Log.d(TAG, "Profile data found — populating UI");
                        fillProfileData(data.results);
                    } else {
                        Toast.makeText(EditProfileActivity.this, "No profile data found", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "data.results is null");
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response error body: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GetProfileResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    private void fillProfileData(GetProfileResponse.Results results) {
        String fullName = ((results.firstName != null ? results.firstName : "") + " " +
                (results.lastName != null ? results.lastName : "")).trim();

        binding.etName.setText(fullName);
        binding.etMobile.setText(results.mobile != null ? results.mobile : "");
        binding.etAddress.setText(results.address != null ? results.address : "");
        binding.etCity.setText(results.city != null ? results.city : "");
        binding.etBloodGroup.setText(results.bloodGroup != null ? results.bloodGroup : "");
        binding.etDob.setText(results.dob != null ? results.dob : "");

        Log.d(TAG, "Profile Loaded:");
        Log.d(TAG, "Name: " + fullName);
        Log.d(TAG, "Mobile: " + results.mobile);
        Log.d(TAG, "Address: " + results.address);
        Log.d(TAG, "City: " + results.city);
        Log.d(TAG, "Blood Group: " + results.bloodGroup);
        Log.d(TAG, "DOB: " + results.dob);
        Log.d(TAG, "Profile Image: " + results.profile);

        if (results.profile != null && !results.profile.isEmpty()) {
            String imageUrl = "http://164.52.197.192:5678/uploads/" + results.profile;
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.log)
                    .into(binding.imgProfile);
        } else {
            binding.imgProfile.setImageResource(R.drawable.log);
        }
    }

    private void setupUI() {
        binding.tvChangePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }
}
