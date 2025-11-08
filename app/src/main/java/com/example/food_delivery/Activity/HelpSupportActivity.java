package com.example.food_delivery.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.HelpSupportResponse;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityHelpSupportBinding;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelpSupportActivity extends AppCompatActivity {

    private ActivityHelpSupportBinding binding;
    private OtpApi otpApi;
    private static final String TAG = "HelpSupportActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpSupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        otpApi = ApiClient.getClient().create(OtpApi.class);

        binding.toolbarHelpSupport.setNavigationOnClickListener(v -> onBackPressed());

        binding.btnSubmitRequest.setOnClickListener(v -> {
            String description = binding.etIssueDescription.getText().toString().trim();

            if (description.isEmpty()) {
                Toast.makeText(this, "Please describe your issue", Toast.LENGTH_SHORT).show();
            } else {
                sendSupportRequest(description);
            }
        });
    }

    private void sendSupportRequest(String description) {
        String token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token not found. Please login again.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Token is null or empty");
            return;
        }


        binding.progressLoader.setVisibility(View.VISIBLE);
        binding.btnSubmitRequest.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("description", description);

        Log.d(TAG, "🟢 Sending request: " + body);
        Log.d(TAG, "🟣 Token: " + token);

        Call<HelpSupportResponse> call = otpApi.supportDelivery("Bearer " + token, body);
        call.enqueue(new Callback<HelpSupportResponse>() {
            @Override
            public void onResponse(Call<HelpSupportResponse> call, Response<HelpSupportResponse> response) {
                binding.progressLoader.setVisibility(View.GONE);
                binding.btnSubmitRequest.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    HelpSupportResponse data = response.body();

                    Log.d(TAG, "✅ Response: success=" + data.isSuccess() +
                            ", message=" + data.getMessage());

                    Toast.makeText(HelpSupportActivity.this, data.getMessage(), Toast.LENGTH_LONG).show();
                    onBackPressed();
                } else {
                    Log.e(TAG, "❌ Response failed: " + response.code() + " " + response.message());
                    Toast.makeText(HelpSupportActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HelpSupportResponse> call, Throwable t) {
                binding.progressLoader.setVisibility(View.GONE);
                binding.btnSubmitRequest.setEnabled(true);

                Log.e(TAG, "🔥 API Error: " + t.getMessage(), t);
                Toast.makeText(HelpSupportActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
