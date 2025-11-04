package com.example.food_delivery.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.food_delivery.Adapter.OrderHistoryAdapter;
import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.OrderHistoryResponse;
import com.example.food_delivery.Model.OrderModel;
import com.example.food_delivery.R;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityOrderHistoryAcitivityBinding;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryAcitivity extends AppCompatActivity {

    public ActivityOrderHistoryAcitivityBinding binding;
    private List<OrderHistoryResponse.OrderData> orderList= new ArrayList();
    private OrderHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOrderHistoryAcitivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        // Attach empty adapter first
        adapter = new OrderHistoryAdapter(orderList);
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrderHistory.setAdapter(adapter);

        // Then load data
        getApiCall();

    }

    private void getApiCall() {
        OtpApi api= ApiClient.getClient().create(OtpApi.class);
        String token= DocumentPrefs.getToken(this);

        Call<OrderHistoryResponse> call = api.getHistory("Bearer " + token);
        call.enqueue(new Callback<OrderHistoryResponse>() {
            @Override
            public void onResponse(Call<OrderHistoryResponse> call, Response<OrderHistoryResponse> response) {
                Log.d("order history", "Response: " + new Gson().toJson(response.body()));
                if (response.isSuccessful() && response.body() != null) {
                    OrderHistoryResponse result = response.body();

                    // ✅ SAFETY CHECK
                    if (result.getResults() != null && result.getResults().getData() != null) {
                        orderList.clear();
                        orderList.addAll(result.getResults().getData());
                        setupAdapter();

                        if (orderList.isEmpty()) {
                            binding.layoutNoOrders.setVisibility(View.VISIBLE);
                            binding.rvOrderHistory.setVisibility(View.GONE);
                        } else {
                            binding.layoutNoOrders.setVisibility(View.GONE);
                            binding.rvOrderHistory.setVisibility(View.VISIBLE);
                        }

                    } else {
                        Log.e("order history", "API failed with code: " + response.code());
                        binding.layoutNoOrders.setVisibility(View.VISIBLE);
                        binding.rvOrderHistory.setVisibility(View.GONE);

                        if (response.errorBody() != null) {
                            try {
                                Log.e("order history", "Error: " + response.errorBody().string());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
                else {
                    System.out.println("⚠️ API Failed - Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            System.out.println("⚠️ Error Body: " + response.errorBody().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderHistoryResponse> call, Throwable t) {

            }
        });
    }

    private void setupAdapter() {
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter= new OrderHistoryAdapter(orderList);
        binding.rvOrderHistory.setAdapter(adapter);
    }
}