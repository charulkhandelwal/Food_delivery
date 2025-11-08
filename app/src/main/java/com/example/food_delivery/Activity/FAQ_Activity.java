package com.example.food_delivery.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.food_delivery.Adapter.FAQAdapter;
import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.FAQModel;
import com.example.food_delivery.Model.GetFaqResponse;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityFaqBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FAQ_Activity extends AppCompatActivity {

    private ActivityFaqBinding binding;
    private FAQAdapter adapter;
    private final List<FAQModel> faqList = new ArrayList<>();
    private static final String TAG = "FAQ_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaqBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerFaq.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FAQAdapter(faqList);
        binding.recyclerFaq.setAdapter(adapter);

        getFaqData();
    }

    private void getFaqData() {
        //binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvTitle.setText("Loading FAQs...");

        String token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token not found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "🔑 Token: " + token);

        OtpApi api = ApiClient.getClient().create(OtpApi.class);
        Call<GetFaqResponse> call = api.getFaq("Bearer " + token);

        call.enqueue(new Callback<GetFaqResponse>() {
            @Override
            public void onResponse(Call<GetFaqResponse> call, Response<GetFaqResponse> response) {
               // binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    GetFaqResponse faqResponse = response.body();
                    Log.d(TAG, "✅ Full Response: " + faqResponse);

                    if (faqResponse.results != null && !faqResponse.results.isEmpty()) {
                        faqList.clear();
                        for (GetFaqResponse.Result result : faqResponse.results) {
                            faqList.add(new FAQModel(result.title, result.content));
                        }
                        adapter.notifyDataSetChanged();
                        binding.tvTitle.setText("FAQs (" + faqList.size() + ")");
                    } else {
                        binding.tvTitle.setText("No FAQs Found");
                        Toast.makeText(FAQ_Activity.this, "No FAQ data found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Response Failed: " + response.code() + " " + response.message());
                    binding.tvTitle.setText("Error Fetching FAQs");
                    Toast.makeText(FAQ_Activity.this, "Failed to load FAQs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetFaqResponse> call, Throwable t) {
               // binding.progressBar.setVisibility(View.GONE);
                binding.tvTitle.setText("Error Loading FAQs");
                Log.e(TAG, "🚨 API Error: " + t.getMessage(), t);
                Toast.makeText(FAQ_Activity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
