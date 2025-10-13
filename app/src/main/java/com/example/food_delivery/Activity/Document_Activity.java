package com.example.food_delivery.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.DocumentModel;
import com.example.food_delivery.Model.DocumentResponse;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityDocumentBinding;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Document_Activity extends AppCompatActivity {

    private ActivityDocumentBinding binding;
    private ArrayList<DocumentModel> documentList;

    private final ActivityResultLauncher<Intent> documentLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    documentList = DocumentPrefs.getDocumentList(this);
                    checkDocsStatus();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        documentList = DocumentPrefs.getDocumentList(this);
        checkDocsStatus();

        binding.pendingPersonal.setOnClickListener(v -> {
            Intent intent = new Intent(this, personal_Document.class);
            documentLauncher.launch(intent);
        });

        binding.pendingVehicle.setOnClickListener(v -> {
            Intent intent = new Intent(this, VehicleDetails_Activity.class);
            documentLauncher.launch(intent);
        });

        binding.pendingBank.setOnClickListener(v -> {
            Intent intent = new Intent(this, BankAccount_Activity.class);
            documentLauncher.launch(intent);
        });

        binding.backiv1.setOnClickListener(v -> binding.pendingPersonal.performClick());
        binding.backiv2.setOnClickListener(v -> binding.pendingVehicle.performClick());
        binding.back3.setOnClickListener(v -> binding.pendingBank.performClick());

        binding.btnSubmit.setOnClickListener(v -> {
            /*if (isAllDocumentsUploaded()) {
                uploadDocumentsToServer();
            } else {
                Toast.makeText(this, "Please upload all documents before submitting", Toast.LENGTH_SHORT).show();
            }*/
            uploadDocumentsToServer();
        });
    }
    private void uploadDocumentsToServer() {

        String token = DocumentPrefs.getToken(this);
        Log.d("UPLOAD_DOCS", "Token: " + token);

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);


        File aadhaarFrontFile = new File(getFilesDir(), "aadhaar_front.jpg");
        File aadhaarBackFile = new File(getFilesDir(), "aadhaar_back.jpg");

        Log.d("UPLOAD_CHECK", "Aadhaar Front Path: " + aadhaarFrontFile.getAbsolutePath() +
                " | Exists: " + aadhaarFrontFile.exists() +
                " | Size: " + (aadhaarFrontFile.exists() ? aadhaarFrontFile.length() : 0) + " bytes");

        Log.d("UPLOAD_CHECK", "Aadhaar Back Path: " + aadhaarBackFile.getAbsolutePath() +
                " | Exists: " + aadhaarBackFile.exists() +
                " | Size: " + (aadhaarBackFile.exists() ? aadhaarBackFile.length() : 0) + " bytes");


        File panFrontFile = new File(getFilesDir(), "pan_front.jpg");
        File panBackFile = new File(getFilesDir(), "pan_back.jpg");

        Log.d("UPLOAD_CHECK", "PAN Front Path: " + panFrontFile.getAbsolutePath() +
                " | Exists: " + panFrontFile.exists());
        Log.d("UPLOAD_CHECK", "PAN Back Path: " + panBackFile.getAbsolutePath() +
                " | Exists: " + panBackFile.exists());


        File dlFrontFile = new File(getFilesDir(), "dl_front.jpg");
        File dlBackFile = new File(getFilesDir(), "dl_back.jpg");

        Log.d("UPLOAD_CHECK", "DL Front Path: " + dlFrontFile.getAbsolutePath() +
                " | Exists: " + dlFrontFile.exists());
        Log.d("UPLOAD_CHECK", "DL Back Path: " + dlBackFile.getAbsolutePath() +
                " | Exists: " + dlBackFile.exists());


        File rcFrontFile = new File(getFilesDir(), "rc_front.jpg");
        File rcBackFile = new File(getFilesDir(), "rc_back.jpg");

        Log.d("UPLOAD_CHECK", "RC Front Path: " + rcFrontFile.getAbsolutePath() +
                " | Exists: " + rcFrontFile.exists());
        Log.d("UPLOAD_CHECK", "RC Back Path: " + rcBackFile.getAbsolutePath() +
                " | Exists: " + rcBackFile.exists());


        MultipartBody.Part aadhaarFront = prepareFilePart("aadhaarFront", aadhaarFrontFile);
        MultipartBody.Part aadhaarBack = prepareFilePart("aadhaarBack", aadhaarBackFile);
        MultipartBody.Part panFront = prepareFilePart("panFront", panFrontFile);
        MultipartBody.Part panBack = prepareFilePart("panBack", panBackFile);
        MultipartBody.Part dlFront = prepareFilePart("drivingLicenseFront", dlFrontFile);
        MultipartBody.Part dlBack = prepareFilePart("drivingLicenseBack", dlBackFile);
        MultipartBody.Part rcFront = prepareFilePart("rcFront", rcFrontFile);
        MultipartBody.Part rcBack = prepareFilePart("rcBack", rcBackFile);


        Log.d("UPLOAD_SUMMARY", "🧾 IMAGE PARTS STATUS:");
        Log.d("UPLOAD_SUMMARY", "AadhaarFront: " + (aadhaarFront != null ? "✅" : "⚠️ NULL"));
        Log.d("UPLOAD_SUMMARY", "AadhaarBack: " + (aadhaarBack != null ? "✅" : "⚠️ NULL"));
        Log.d("UPLOAD_SUMMARY", "PANFront: " + (panFront != null ? "✅" : "⚠️ NULL"));
        Log.d("UPLOAD_SUMMARY", "PANBack: " + (panBack != null ? "✅" : "⚠️ NULL"));
        Log.d("UPLOAD_SUMMARY", "DLFront: " + (dlFront != null ? "✅" : "⚠️ NULL"));
        Log.d("UPLOAD_SUMMARY", "DLBack: " + (dlBack != null ? "✅" : "⚠️ NULL"));
        Log.d("UPLOAD_SUMMARY", "RCFront: " + (rcFront != null ? "✅" : "⚠️ NULL"));
        Log.d("UPLOAD_SUMMARY", "RCBack: " + (rcBack != null ? "✅" : "⚠️ NULL"));


        String accountNo = "1234567890";
        String ifsc = "SBIN0001234";
        String holderName = "Ravi Sharma";

        Log.d("UPLOAD_CHECK", "Account Number: " + (accountNo != null ? accountNo : "⚠️ NULL"));
        Log.d("UPLOAD_CHECK", "IFSC Code: " + (ifsc != null ? ifsc : "⚠️ NULL"));
        Log.d("UPLOAD_CHECK", "Account Holder Name: " + (holderName != null ? holderName : "⚠️ NULL"));

        RequestBody accountNumber = RequestBody.create(MediaType.parse("text/plain"), accountNo);
        RequestBody ifscCode = RequestBody.create(MediaType.parse("text/plain"), ifsc);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), holderName);

        Log.d("UPLOAD_SUMMARY", "✅ TEXT DATA READY: Account=" + accountNo + ", IFSC=" + ifsc + ", Name=" + holderName);


        Log.d("UPLOAD_SUMMARY", "🚀 Starting API call to uploadDocuments()...");

        Call<DocumentResponse> call = api.uploadDocuments(
                aadhaarFront, aadhaarBack,
                panFront, panBack,
                dlFront, dlBack,
                rcFront, rcBack,
                accountNumber, ifscCode, name
        );

        call.enqueue(new Callback<DocumentResponse>() {
            @Override
            public void onResponse(Call<DocumentResponse> call, Response<DocumentResponse> response) {
                Log.d("UPLOAD_API", "Response received: code = " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("UPLOAD_API", "✅ Upload Success: " + response.body().toString());
                    Toast.makeText(Document_Activity.this, "Documents uploaded successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Document_Activity.this, OrderSuccess.class));
                    finish();
                } else {
                    Log.e("UPLOAD_API", "❌ Upload Failed: " + response.code() + " | " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DocumentResponse> call, Throwable t) {
                Log.e("UPLOAD_API", "❌ Error during upload: " + t.getMessage(), t);
            }
        });
    }


    private MultipartBody.Part prepareFilePart(String partName, File file) {
        if (file != null && file.exists()) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
        } else {
            return null;
        }
    }

    private void checkDocsStatus() {
        boolean aadhaarUploaded = false;
        boolean panUploaded = false;
        boolean dlUploaded = false;
        boolean vehicleUploaded = false;
        boolean bankUploaded = false;

        for (DocumentModel model : documentList) {
            switch (model.getDocName()) {
                case "aadhaar":
                    aadhaarUploaded = true;
                    break;
                case "pan":
                    panUploaded = true;
                    break;
                case "dl":
                    dlUploaded = true;
                    break;
                case "vehicle_front":
                case "vehicle_back":
                    vehicleUploaded = true;
                    break;
                case "bank":
                    bankUploaded = true;
                    break;
            }
        }

        binding.completedPersonal.setVisibility(aadhaarUploaded && panUploaded && dlUploaded ? View.VISIBLE : View.GONE);
        binding.pendingPersonal.setVisibility(aadhaarUploaded && panUploaded && dlUploaded ? View.GONE : View.VISIBLE);

        binding.completedVehicle.setVisibility(vehicleUploaded ? View.VISIBLE : View.GONE);
        binding.pendingVehicle.setVisibility(vehicleUploaded ? View.GONE : View.VISIBLE);

        binding.completedBank.setVisibility(bankUploaded ? View.VISIBLE : View.GONE);
        binding.pendingBank.setVisibility(bankUploaded ? View.GONE : View.VISIBLE);
    }

    private boolean isAllDocumentsUploaded() {
        boolean aadhaarUploaded = false;
        boolean panUploaded = false;
        boolean dlUploaded = false;
        boolean vehicleUploaded = false;
        boolean bankUploaded = false;

        for (DocumentModel model : documentList) {
            switch (model.getDocName()) {
                case "aadhaar":
                    aadhaarUploaded = true;
                    break;
                case "pan":
                    panUploaded = true;
                    break;
                case "dl":
                    dlUploaded = true;
                    break;
                case "vehicle_front":
                case "vehicle_back":
                    vehicleUploaded = true;
                    break;
                case "bank":
                    bankUploaded = true;
                    break;
            }
        }

        return aadhaarUploaded && panUploaded && dlUploaded && vehicleUploaded && bankUploaded;
    }
}
