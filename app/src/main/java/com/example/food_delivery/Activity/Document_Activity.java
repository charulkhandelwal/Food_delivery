package com.example.food_delivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.DocumentGetResponse;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityDocumentBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Document_Activity extends AppCompatActivity {

    private ActivityDocumentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getDocumentsFromServer();

        binding.pendingPersonal5.setOnClickListener(v -> openUploadScreen("aadhar"));
        binding.pendingPersonal2.setOnClickListener(v -> openUploadScreen("pan"));
        binding.pendingPersonal.setOnClickListener(v -> openUploadScreen("drivingLicence"));
        binding.pendingVehicle.setOnClickListener(v -> openUploadScreen("rc"));
        binding.pendingBank.setOnClickListener(v -> openBankScreen("bankDetails"));


        binding.btnSubmit.setOnClickListener(v -> checkAllApprovedAndProceed());
    }

    private void openUploadScreen(String docType) {
        Intent intent = new Intent(this, Aadharcard_Activity.class);
        intent.putExtra("docType", docType);
        startActivity(intent);
    }

    private void openBankScreen(String docType) {
        Intent intent = new Intent(this, BankAccount_Activity.class);
        intent.putExtra("docType", docType);
        startActivity(intent);
    }


    private void checkAllApprovedAndProceed() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String token = DocumentPrefs.getToken(this);

        if (token.isEmpty()) {
            Toast.makeText(this, "Missing token! Please log in again.", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        Call<DocumentGetResponse> call = api.getdocuments();

        call.enqueue(new Callback<DocumentGetResponse>() {
            @Override
            public void onResponse(Call<DocumentGetResponse> call, Response<DocumentGetResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    DocumentGetResponse.Documents docs = response.body().getResults().documents;

                    String aadharStatus = docs.aadhar != null ? docs.aadhar.status : "pending";
                    String panStatus = docs.pan != null ? docs.pan.status : "pending";
                    String dlStatus = docs.drivingLicence != null ? docs.drivingLicence.status : "pending";
                    String rcStatus = docs.rc != null ? docs.rc.status : "pending";
                    String bankStatus = docs.bankAccountDetails != null ? docs.bankAccountDetails.status : "pending";

                    boolean allApproved =
                            aadharStatus.equalsIgnoreCase("approved") &&
                                    panStatus.equalsIgnoreCase("approved") &&
                                    dlStatus.equalsIgnoreCase("approved") &&
                                    rcStatus.equalsIgnoreCase("approved") &&
                                    bankStatus.equalsIgnoreCase("approved");

                    if (allApproved) {
                        Toast.makeText(Document_Activity.this, "✅ All documents approved!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Document_Activity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(Document_Activity.this, "Some documents are still pending or rejected.", Toast.LENGTH_SHORT).show();
                        getDocumentsFromServer(); // Refresh UI
                    }

                } else {
                    Toast.makeText(Document_Activity.this, "Failed to fetch document status.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DocumentGetResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(Document_Activity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDocumentsFromServer() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String token = DocumentPrefs.getToken(this);

        if (token.isEmpty()) {
            Toast.makeText(this, "Missing token! Please log in again.", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        Call<DocumentGetResponse> call = api.getdocuments();

        Log.d("DOC_API", "Fetching documents with token: " + token);

        call.enqueue(new Callback<DocumentGetResponse>() {
            @Override
            public void onResponse(Call<DocumentGetResponse> call, Response<DocumentGetResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateUI(response.body().getResults().documents);
                    Log.d("DOC_API", "Response: " + response.body().getMessage());
                } else {
                    Log.e("DOC_API", "Error response: " + response.code());
                    Toast.makeText(Document_Activity.this, "Failed to fetch documents", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DocumentGetResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e("DOC_API", "API failed: " + t.getMessage());
                Toast.makeText(Document_Activity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(DocumentGetResponse.Documents documents) {
        setStatus(binding.tvPersonalStatus, documents.aadhar != null ? documents.aadhar.status : null);
        setStatus(binding.tvPersonalStatus1, documents.pan != null ? documents.pan.status : null);
        setStatus(binding.tvPersonalStatus3, documents.drivingLicence != null ? documents.drivingLicence.status : null);
        setStatus(binding.tvVehicleStatus, documents.rc != null ? documents.rc.status : null);
        setStatus(binding.tvBankStatus, documents.bankAccountDetails != null ? documents.bankAccountDetails.status : null);
    }

    private void setStatus(android.widget.TextView textView, String status) {
        if (status == null) status = "pending";
        switch (status.toLowerCase()) {
            case "uploaded":
                textView.setText("Uploaded ✅");
                textView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case "approved":
                textView.setText("Approved ✅");
                textView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "rejected":
                textView.setText("Rejected ❌");
                textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                textView.setText("Pending ⏳");
                textView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDocumentsFromServer();
    }
}
