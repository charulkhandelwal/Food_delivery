package com.example.food_delivery.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.DocumentGetResponse;
import com.example.food_delivery.Model.DocumentResponse;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityBankAccountBinding;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BankAccount_Activity extends AppCompatActivity {

    private ActivityBankAccountBinding binding;
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBankAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        token = DocumentPrefs.getToken(this);

        binding.etIfscCode.setOnClickListener(v -> finish());


        getBankDetails();

        binding.btnSubmitBankDetails.setOnClickListener(v -> uploadBankDetails());
    }


    private void getBankDetails() {
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Missing token! Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }


        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);

        api.getdocuments().enqueue(new Callback<DocumentGetResponse>() {
            @Override
            public void onResponse(Call<DocumentGetResponse> call, Response<DocumentGetResponse> response) {
               // binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    DocumentGetResponse.Documents docs = response.body().getResults().documents;
                    if (docs != null && docs.bankAccountDetails != null) {
                        binding.etAccountHolderName.setText(docs.bankAccountDetails.name != null ? docs.bankAccountDetails.name : "");
                        binding.etAccountNumber.setText(
                                String.valueOf(docs.bankAccountDetails.accountNumber != null ? docs.bankAccountDetails.accountNumber : "")
                        );
                        binding.etIfscCode.setText(docs.bankAccountDetails.ifscCode != null ? docs.bankAccountDetails.ifscCode : "");
                        Log.d("BANK_GET", "✅ Bank data fetched successfully");
                    } else {
                        Log.d("BANK_GET", "⚠️ No bank data found");
                    }
                } else {
                    Toast.makeText(BankAccount_Activity.this, "Failed to fetch bank details", Toast.LENGTH_SHORT).show();
                    Log.e("BANK_GET_ERROR", "Response Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DocumentGetResponse> call, Throwable t) {
                //binding.progressBar.setVisibility(View.GONE);
                Log.e("BANK_GET_FAIL", "Error: " + t.getMessage());
                Toast.makeText(BankAccount_Activity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void uploadBankDetails() {
        String holderName = binding.etAccountHolderName.getText().toString().trim();
        String accountNumber = binding.etAccountNumber.getText().toString().trim();
        String ifscCode = binding.etIfscCode.getText().toString().trim();

        if (holderName.isEmpty() || accountNumber.isEmpty() || ifscCode.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Missing token! Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading bank details...");
        dialog.setCancelable(false);
        dialog.show();


        Map<String, RequestBody> map = new HashMap<>();
        map.put("docType", createPartFromString("bankDetails"));
        map.put("name", createPartFromString(holderName));
        map.put("accountNumber", createPartFromString(accountNumber));
        map.put("ifscCode", createPartFromString(ifscCode));

        // Dummy empty part list because API expects form-data
        MultipartBody.Part dummyFile = MultipartBody.Part.createFormData("bankDetailsFront", "", RequestBody.create(MediaType.parse("image/*"), new byte[0]));

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        api.uploadDocuments(map, java.util.Collections.singletonList(dummyFile)).enqueue(new Callback<DocumentResponse>() {
            @Override
            public void onResponse(Call<DocumentResponse> call, Response<DocumentResponse> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(BankAccount_Activity.this, "Bank details uploaded successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("BANK_UPLOAD", "✅ Success");
                    getBankDetails(); // Refresh data
                } else {
                    Toast.makeText(BankAccount_Activity.this, "Upload failed! Try again.", Toast.LENGTH_SHORT).show();
                    Log.e("BANK_UPLOAD", "❌ Failed - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DocumentResponse> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(BankAccount_Activity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("BANK_UPLOAD_FAIL", "Error: " + t.getMessage());
            }
        });
    }

    private RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}
