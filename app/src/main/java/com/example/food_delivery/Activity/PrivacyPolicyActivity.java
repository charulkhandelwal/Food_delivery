package com.example.food_delivery.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.PrivacyPolicyModelResponse;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityPrivacyPolicyBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ActivityPrivacyPolicyBinding binding;
    private OtpApi otpApi;
    private static final String TAG = "PrivacyPolicyActivity";
    private String type; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        otpApi = ApiClient.getClient().create(OtpApi.class);
        type = getIntent().getStringExtra("type");


        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if ("terms".equalsIgnoreCase(type)) {
            binding.toolbar.setTitle("Terms & Conditions");
            getTermsConditions();
        } else {
            binding.toolbar.setTitle("Privacy Policy");
            getPrivacyPolicy();
        }
    }

    private void getPrivacyPolicy() {
        String token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token missing. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        Call<PrivacyPolicyModelResponse> call = otpApi.getPrivacy("Bearer " + token);
        call.enqueue(new Callback<PrivacyPolicyModelResponse>() {
            @Override
            public void onResponse(Call<PrivacyPolicyModelResponse> call, Response<PrivacyPolicyModelResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    PrivacyPolicyModelResponse data = response.body();


                    Log.d(TAG, "API Response: " + new com.google.gson.Gson().toJson(data));

                    if (data.results != null) {
                        binding.tvDescription.setText(data.results.description);

                        Log.d(TAG, "Privacy Policy Title: " + data.results.title);
                        Log.d(TAG, "Privacy Policy Description: " + data.results.description);
                    } else {
                        Toast.makeText(PrivacyPolicyActivity.this, "No data found.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "No results in response");
                    }

                } else {
                    Toast.makeText(PrivacyPolicyActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PrivacyPolicyModelResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(PrivacyPolicyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API Error", t);
            }
        });
    }

    private void getTermsConditions() {
        String token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token missing. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        Call<PrivacyPolicyModelResponse> call = otpApi.getTerms("Bearer " + token);
        call.enqueue(new Callback<PrivacyPolicyModelResponse>() {
            @Override
            public void onResponse(Call<PrivacyPolicyModelResponse> call, Response<PrivacyPolicyModelResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    PrivacyPolicyModelResponse data = response.body();


                    Log.d(TAG, "API Response: " + new com.google.gson.Gson().toJson(data));

                    if (data.results != null) {
                        binding.tvDescription.setText(data.results.description);

                        Log.d(TAG, "Terms Title: " + data.results.title);
                        Log.d(TAG, "Terms Description: " + data.results.description);
                    } else {
                        Toast.makeText(PrivacyPolicyActivity.this, "No data found.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "No results in response");
                    }

                } else {
                    Toast.makeText(PrivacyPolicyActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PrivacyPolicyModelResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(PrivacyPolicyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API Error", t);
            }
        });
    }
}
