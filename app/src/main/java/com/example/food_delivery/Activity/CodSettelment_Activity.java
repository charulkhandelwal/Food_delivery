package com.example.food_delivery.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.food_delivery.Adapter.CodSettelmentAdapter;
import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.WalletSettleResponse;
import com.example.food_delivery.R;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityCodSettelmentBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CodSettelment_Activity extends AppCompatActivity {

    private ActivityCodSettelmentBinding binding;
    private List<WalletSettleResponse.WalletTransaction> codList;
    private CodSettelmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // ✅ Initialize ViewBinding
        binding = ActivityCodSettelmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecycler();
        callGetApi();
        setupPayButton();
    }

    // ✅ Setup RecyclerView
    private void setupRecycler() {
        binding.recyclerCodSettlement.setLayoutManager(new LinearLayoutManager(this));
        codList = new ArrayList<>();
        adapter = new CodSettelmentAdapter(this, codList);
        binding.recyclerCodSettlement.setAdapter(adapter);
    }

    // ✅ Call API
    private void callGetApi() {
        String token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token missing. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        Call<WalletSettleResponse> call = api.getCODSettlement("Bearer " + token);

        call.enqueue(new Callback<WalletSettleResponse>() {
            @Override
            public void onResponse(Call<WalletSettleResponse> call, Response<WalletSettleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WalletSettleResponse data = response.body();
                    if (data.isSuccess() && data.getResults() != null && data.getResults().getAllData() != null) {
                        codList.clear();
                        codList.addAll(data.getResults().getAllData());

                        adapter.notifyDataSetChanged();
                        calculateSummary(data);
                    } else {
                        binding.summaryLayout.setVisibility(View.GONE);
                        Toast.makeText(CodSettelment_Activity.this, "No settlement data found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CodSettelment_Activity.this, "Failed to fetch settlement data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WalletSettleResponse> call, Throwable t) {
                Toast.makeText(CodSettelment_Activity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Calculate Total, Commission & Final Amount
    private void calculateSummary(WalletSettleResponse data) {
        double totalPayment = data.getResults().getTotalAmount();
        double totalCommission = data.getResults().getDeliveryManCommission();
        double toPay = totalPayment - totalCommission;

        binding.tvTotalPayment.setText("Total Payment: ₹" + totalPayment);
        binding.tvCommission.setText("Commission: ₹" + totalCommission);
        binding.tvToPayCalculation.setText("₹" + totalPayment + " - ₹" + totalCommission);
        binding.tvFinalAmount.setText("Final Amount: ₹" + toPay);
    }

    // ✅ Pay Button Action
    private void setupPayButton() {
        binding.btnPay.setOnClickListener(v -> {
            String amount = binding.tvFinalAmount.getText().toString().replace("Final Amount: ₹", "");
//            Toast.makeText(this, "Payment of ₹" + amount + " Successful ✅", Toast.LENGTH_SHORT).show();

            callHitPaymentApi(amount);
        });
    }

    private void callHitPaymentApi(String amount) {
        String token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token missing. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Example transaction IDs (you’ll get them from your order list)
        List<String> transactionIds = new ArrayList<>();
        for (WalletSettleResponse.WalletTransaction item : codList) {
            transactionIds.add(item.get_id());
        }
        // Prepare request body
        Map<String, Object> body = new HashMap<>();
        body.put("amount", Double.parseDouble(amount)); // ✅ double value
        body.put("transactionIds", transactionIds);     // ✅ list of strings

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        Call<WalletSettleResponse> call = api.postCODSettlement("Bearer " + token, (Map) body);

        call.enqueue(new Callback<WalletSettleResponse>() {
            @Override
            public void onResponse(Call<WalletSettleResponse> call, Response<WalletSettleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
//                    Toast.makeText(CodSettelment_Activity.this, "Settlement initiated successfully", Toast.LENGTH_SHORT).show();

                    // ✅ Now open UPI app for payment
                    openUpiApp(amount);
                } else {
                    Toast.makeText(CodSettelment_Activity.this, "Settlement failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WalletSettleResponse> call, Throwable t) {
                Toast.makeText(CodSettelment_Activity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openUpiApp(String amount) {
        String upiId = "admin@upi";  // ✅ Replace with your admin UPI ID
        String payeeName = "Admin Account";
        String note = "COD Settlement Payment";

        Uri uri = Uri.parse("upi://pay").buildUpon().appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", payeeName).
                appendQueryParameter("tn", note).appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayment = new Intent(Intent.ACTION_VIEW);
        upiPayment.setData(uri);
        // ✅ Let the user pick UPI app
        Intent chooser = Intent.createChooser(upiPayment, "Pay with UPI");
        try {
            startActivityForResult(chooser, 1001);
        } catch (Exception e) {
            Toast.makeText(this, "No UPI app found. Please install one to proceed.", Toast.LENGTH_LONG).show();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (data != null) {
                String response = data.getStringExtra("response");
                if (response != null && response.toLowerCase().contains("success")) {
                    Toast.makeText(this, "UPI Payment Successful ✅", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "UPI Payment Failed ❌", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
