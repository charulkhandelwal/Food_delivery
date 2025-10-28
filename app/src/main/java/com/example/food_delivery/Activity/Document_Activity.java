package com.example.food_delivery.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.DocumentGetResponse;
import com.example.food_delivery.Model.DocumentModel;
import com.example.food_delivery.Model.DocumentResponse;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityDocumentBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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

        getDocumentsFromServer();

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
            if (isAllDocumentsUploaded()) {
                uploadDocumentsToServer();
            } else {
                Toast.makeText(this, "Please upload all documents before submitting", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getDocumentsFromServer() {
        String token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) {
            Log.w("GET_DOCS", "Token missing, aborting getDocumentsFromServer");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        Call<DocumentGetResponse> call = api.getdocuments();

        call.enqueue(new Callback<DocumentGetResponse>() {
            @Override
            public void onResponse(Call<DocumentGetResponse> call, Response<DocumentGetResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<DocumentModel> docs = response.body().getDocuments();
                    Log.d("GET_DOCS", "Fetched docs count: " + (docs == null ? 0 : docs.size()));

                    if (docs != null) {
                        // docs are already DocumentModel instances created in DocumentGetResponse.getDocuments()
                        documentList = new ArrayList<>(docs);
                        DocumentPrefs.saveDocumentList(Document_Activity.this, documentList);
                        checkDocsStatus();
                    } else {
                        Log.w("GET_DOCS", "No documents in response body");
                    }
                } else {
                    Log.e("GET_DOCS", "GET documents failed, code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DocumentGetResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e("GET_DOCS", "GET documents error: " + t.getMessage());
            }
        });
    }

    // -------------------------
    // Upload documents to server
    // -------------------------
    private void uploadDocumentsToServer() {
        String token = DocumentPrefs.getToken(this);
        Log.d("UPLOAD_DOCS", "Token: " + token);

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        ArrayList<DocumentModel> savedDocs = DocumentPrefs.getDocumentList(this);

        if (savedDocs == null || savedDocs.isEmpty()) {
            Toast.makeText(this, "No saved documents found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loader and disable submit
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        MultipartBody.Part aadharFront = null, aadharBack = null,
                panFront = null, panBack = null,
                drivingLicenceFront = null, drivingLicenceBack = null,
                rcFront = null, rcBack = null;

        for (DocumentModel doc : savedDocs) {
            try {
                String uriStr = doc.getImageUri();
                if (uriStr == null || uriStr.trim().isEmpty()) {
                    Log.w("UPLOAD_SKIP", "Skipping doc (no URI): " + doc.getDocName());
                    continue;
                }

                // accept both content:// and file:// URIs
                if (!uriStr.startsWith("content://") && !uriStr.startsWith("file://")) {
                    Log.w("UPLOAD_SKIP", "Skipping invalid URI scheme for " + doc.getDocName() + ": " + uriStr);
                    continue;
                }

                Uri uri = Uri.parse(uriStr);
                String tempName = doc.getDocName().toLowerCase().replace(" ", "_") + ".jpg";
                File file = copyUriToInternalStorage(uri, tempName);
                logFileStatus(doc.getDocName(), file);

                MultipartBody.Part part = null;
                String nameLower = doc.getDocName().toLowerCase();

                if (nameLower.contains("aadhaar") && nameLower.contains("front"))
                    part = prepareFilePart("aadharFront", file);
                else if (nameLower.contains("aadhaar") && nameLower.contains("back"))
                    part = prepareFilePart("aadharBack", file);
                else if (nameLower.contains("pan") && nameLower.contains("front"))
                    part = prepareFilePart("panFront", file);
                else if (nameLower.contains("pan") && nameLower.contains("back"))
                    part = prepareFilePart("panBack", file);
                else if (nameLower.contains("dl") && nameLower.contains("front"))
                    part = prepareFilePart("drivingLicenceFront", file);
                else if (nameLower.contains("dl") && nameLower.contains("back"))
                    part = prepareFilePart("drivingLicenceBack", file);
                else if (nameLower.contains("vehicle") && nameLower.contains("front"))
                    part = prepareFilePart("rcFront", file);
                else if (nameLower.contains("vehicle") && nameLower.contains("back"))
                    part = prepareFilePart("rcBack", file);

                if (part != null) {
                    if (nameLower.contains("aadhaar") && nameLower.contains("front")) aadharFront = part;
                    else if (nameLower.contains("aadhaar") && nameLower.contains("back")) aadharBack = part;
                    else if (nameLower.contains("pan") && nameLower.contains("front")) panFront = part;
                    else if (nameLower.contains("pan") && nameLower.contains("back")) panBack = part;
                    else if (nameLower.contains("dl") && nameLower.contains("front")) drivingLicenceFront = part;
                    else if (nameLower.contains("dl") && nameLower.contains("back")) drivingLicenceBack = part;
                    else if (nameLower.contains("vehicle") && nameLower.contains("front")) rcFront = part;
                    else if (nameLower.contains("vehicle") && nameLower.contains("back")) rcBack = part;
                }

            } catch (Exception e) {
                Log.e("UPLOAD_ERROR", "Error preparing file for " + doc.getDocName() + " : " + e.getMessage());
            }
        }

        // Dummy bank details (replace with real values)
        RequestBody accountNumber = RequestBody.create(MediaType.parse("text/plain"), "1234567890");
        RequestBody ifscCode = RequestBody.create(MediaType.parse("text/plain"), "SBIN0001234");
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "Ravi Sharma");

        Call<DocumentResponse> call = api.uploadDocuments(
                aadharFront, aadharBack,
                panFront, panBack,
                drivingLicenceFront, drivingLicenceBack,
                rcFront, rcBack,
                accountNumber, ifscCode, name
        );

        call.enqueue(new Callback<DocumentResponse>() {
            @Override
            public void onResponse(Call<DocumentResponse> call, Response<DocumentResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmit.setEnabled(true);

                Log.d("UPLOAD_API", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(Document_Activity.this, "✅ Documents uploaded successfully!", Toast.LENGTH_SHORT).show();
                    DocumentPrefs.setDocsUploaded(Document_Activity.this, true);

                    // Refresh verification status from server after upload
                    getDocumentsFromServer();
                } else {
                    Toast.makeText(Document_Activity.this, "Upload failed! code: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("UPLOAD_API", "Error body: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<DocumentResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmit.setEnabled(true);
                Log.e("UPLOAD_API", "Upload failed: " + t.getMessage());
                Toast.makeText(Document_Activity.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------------
    // Helpers
    // -------------------------
    private File copyUriToInternalStorage(Uri uri, String fileName) {
        File file = new File(getFilesDir(), fileName);
        try (InputStream input = getContentResolver().openInputStream(uri);
             OutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) output.write(buffer, 0, bytesRead);
            Log.d("SAVE_URI", "Copied " + fileName + " -> " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e("SAVE_URI", "Error copying URI to internal storage: " + e.getMessage());
        }
        return file;
    }

    private MultipartBody.Part prepareFilePart(String partName, File file) {
        if (file != null && file.exists()) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
        } else {
            Log.w("UPLOAD_FILE", "Missing file for part: " + partName);
            return null;
        }
    }

    private void logFileStatus(String tag, File file) {
        if (file != null && file.exists()) {
            Log.d("UPLOAD_CHECK", tag + " found | " + file.getAbsolutePath() + " | sizeKB=" + (file.length() / 1024));
        } else {
            Log.w("UPLOAD_CHECK", tag + " not found");
        }
    }

    // Check local document list (saved) and update completed/pending cards
    private void checkDocsStatus() {
        boolean aadhaarUploaded = false, panUploaded = false, dlUploaded = false,
                vehicleUploaded = false, bankUploaded = false;

        if (documentList != null) {
            for (DocumentModel model : documentList) {
                String name = (model.getDocName() == null) ? "" : model.getDocName().toLowerCase();
                if (name.contains("aadhaar") || name.contains("aadhar")) aadhaarUploaded = true;
                if (name.contains("pan")) panUploaded = true;
                if (name.contains("dl") || name.contains("driving")) dlUploaded = true;
                if (name.contains("vehicle") || name.contains("rc")) vehicleUploaded = true;
                if (name.contains("bank")) bankUploaded = true;
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
        boolean aadhaarUploaded = false, panUploaded = false, dlUploaded = false,
                vehicleUploaded = false, bankUploaded = false;

        if (documentList != null) {
            for (DocumentModel model : documentList) {
                String name = (model.getDocName() == null) ? "" : model.getDocName().toLowerCase();
                if (name.contains("aadhaar") || name.contains("aadhar")) aadhaarUploaded = true;
                if (name.contains("pan")) panUploaded = true;
                if (name.contains("dl") || name.contains("driving")) dlUploaded = true;
                if (name.contains("vehicle") || name.contains("rc")) vehicleUploaded = true;
                if (name.contains("bank")) bankUploaded = true;
            }
        }

        return aadhaarUploaded && panUploaded && dlUploaded && vehicleUploaded && bankUploaded;
    }
}
