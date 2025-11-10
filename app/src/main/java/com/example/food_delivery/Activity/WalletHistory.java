package com.example.food_delivery.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.food_delivery.Adapter.WalletHistoryAdapter;
import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.WalletHistoryModelResponse;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityWalletHistoryBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletHistory extends AppCompatActivity {

    private ActivityWalletHistoryBinding binding;
    private WalletHistoryAdapter adapter;
    private String token = "";
    private static final String TAG = "WalletHistoryActivity"; // 🔹 Log tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();

        token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Missing token! Please login again.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Token missing or invalid!");
            return;
        }

        Log.d(TAG, "Token found: " + token); // 🔹 Check token value
        fetchWalletHistory();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Wallet History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        binding.recyclerWalletHistory.setLayoutManager(new LinearLayoutManager(this));
    }

    private void showLoading(boolean show) {
        if (show) {
            binding.shimmerLayout.setVisibility(View.VISIBLE);
            binding.shimmerLayout.startShimmer();
            binding.recyclerWalletHistory.setVisibility(View.GONE);
            binding.tvEmptyState.setVisibility(View.GONE);
        } else {
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
        }
    }

    private void fetchWalletHistory() {
        showLoading(true);
        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        Call<WalletHistoryModelResponse> call = api.getWalletHistory("Bearer " + token);

        Log.d(TAG, "API Request Started...");

        call.enqueue(new Callback<WalletHistoryModelResponse>() {
            @Override
            public void onResponse(Call<WalletHistoryModelResponse> call, Response<WalletHistoryModelResponse> response) {
                showLoading(false);

                // 🔹 Log full raw response
                Log.d(TAG, "Response Code: " + response.code());

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // 🔹 Pretty print JSON for better readability
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        String prettyJson = gson.toJson(response.body());
                        Log.d(TAG, "✅ API Response:\n" + prettyJson);

                        if (response.body().getResults() != null) {
                            List<WalletHistoryModelResponse.WalletDoc> walletList = response.body().getResults().getDocs();

                            if (walletList != null && !walletList.isEmpty()) {
                                adapter = new WalletHistoryAdapter(WalletHistory.this, walletList);
                                binding.recyclerWalletHistory.setAdapter(adapter);
                                binding.recyclerWalletHistory.setVisibility(View.VISIBLE);
                                binding.tvEmptyState.setVisibility(View.GONE);
                            } else {
                                Log.w(TAG, "⚠️ Wallet list empty");
                                binding.recyclerWalletHistory.setVisibility(View.GONE);
                                binding.tvEmptyState.setVisibility(View.VISIBLE);
                                binding.tvEmptyState.setText("No wallet history found");
                            }
                        } else {
                            Log.w(TAG, "⚠️ Results field is null");
                            showError("Invalid API data (results missing)");
                        }
                    } else {
                        Log.e(TAG, "❌ Response body is null");
                        showError("Empty response from server");
                    }
                } else {
                    Log.e(TAG, "❌ API Error Response: " + response.message());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e(TAG, "Error Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading errorBody: " + e.getMessage());
                    }
                    showError("Failed to load data (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<WalletHistoryModelResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "❌ Network failure: " + t.getMessage(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        binding.recyclerWalletHistory.setVisibility(View.GONE);
        binding.tvEmptyState.setVisibility(View.VISIBLE);
        binding.tvEmptyState.setText(message);
        Toast.makeText(WalletHistory.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
