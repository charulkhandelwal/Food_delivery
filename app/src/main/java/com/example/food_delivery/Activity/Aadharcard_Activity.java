package com.example.food_delivery.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.DocumentGetResponse;
import com.example.food_delivery.Model.DocumentResponse;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityAadharcardBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Aadharcard_Activity extends AppCompatActivity {

    private ActivityAadharcardBinding binding;
    private Uri frontUri, backUri, tempCameraUri;
    private String currentDoc = "", docType = "";
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAadharcardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        docType = getIntent().getStringExtra("docType");
        if (docType == null) docType = "aadhar";

        binding.ivBack.setOnClickListener(v -> finish());
        initLaunchers();

        binding.btnUploadFront.setOnClickListener(v -> selectImage("front"));
        binding.btnUploadBack.setOnClickListener(v -> selectImage("back"));

        // Hide or disable back upload depending on document type
        if (docType.equalsIgnoreCase("pan")) {
            binding.btnUploadBack.setEnabled(false);
            binding.btnUploadBack.setAlpha(0.5f);
            binding.imgBackPreview.setVisibility(View.GONE);
        }

        binding.btnSubmit.setOnClickListener(v -> uploadDocumentsToServer());


        switch (docType.toLowerCase()) {
            case "aadhar":
                binding.tvTitle.setText("Upload Aadhar Card");
                break;
            case "pan":
                binding.tvTitle.setText("Upload PAN Card");
                break;
            case "drivinglicence":
                binding.tvTitle.setText("Upload Driving Licence");
                break;
            case "rc":
                binding.tvTitle.setText("Upload RC Document");
                break;
        }

        // ✅ Fetch uploaded doc if exists
        getUploadedDocuments();
    }

    // ✅ Initialize Launchers
    private void initLaunchers() {
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && tempCameraUri != null) {
                showPreview(currentDoc, tempCameraUri);
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                showPreview(currentDoc, uri);
            }
        });
    }

    // ✅ Select source
    private void selectImage(String type) {
        currentDoc = type;
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                })
                .show();
    }


    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Document_Image");
        tempCameraUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        cameraLauncher.launch(tempCameraUri);
    }


    private void openGallery() {
        galleryLauncher.launch("image/*");
    }


    private void showPreview(String type, Uri uri) {
        if (type.equals("front")) {
            frontUri = uri;
            binding.imgFrontPreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(uri).centerCrop().into(binding.imgFrontPreview);
        } else {
            backUri = uri;
            binding.imgBackPreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(uri).centerCrop().into(binding.imgBackPreview);
        }
    }


    private void uploadDocumentsToServer() {
        String token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token missing! Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (frontUri == null) {
            Toast.makeText(this, "Please select front image first!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading " + docType + "...");
        dialog.setCancelable(false);
        dialog.show();

        Map<String, RequestBody> formFields = new HashMap<>();
        formFields.put("docType", createPartFromString(docType));

        List<MultipartBody.Part> files = new ArrayList<>();
        File frontFile = getFileFromUri(frontUri);
        files.add(prepareFilePart(docType + "Front", frontFile));

        if (backUri != null && !docType.equalsIgnoreCase("pan")) {
            File backFile = getFileFromUri(backUri);
            files.add(prepareFilePart(docType + "Back", backFile));
        }

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        api.uploadDocuments(formFields, files).enqueue(new Callback<DocumentResponse>() {
            @Override
            public void onResponse(Call<DocumentResponse> call, Response<DocumentResponse> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(Aadharcard_Activity.this, "Uploaded successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("UPLOAD_SUCCESS", "Document uploaded: " + docType);
                    getUploadedDocuments();
                } else {
                    Log.e("UPLOAD_FAIL", "Code: " + response.code());
                    Toast.makeText(Aadharcard_Activity.this, "Upload failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DocumentResponse> call, Throwable t) {
                dialog.dismiss();
                Log.e("UPLOAD_ERROR", "Error: " + t.getMessage());
                Toast.makeText(Aadharcard_Activity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getUploadedDocuments() {
        String token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) return;

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        api.getdocuments().enqueue(new Callback<DocumentGetResponse>() {
            @Override
            public void onResponse(Call<DocumentGetResponse> call, Response<DocumentGetResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    DocumentGetResponse.Documents docs = response.body().getResults().documents;
                    if (docs == null) return;

                    String frontUrl = null;
                    String backUrl = null;

                    switch (docType.toLowerCase()) {
                        case "aadhar":
                            if (docs.aadhar != null) {
                                frontUrl = docs.aadhar.front;
                                backUrl = docs.aadhar.back;
                            }
                            break;
                        case "pan":
                            if (docs.pan != null) {
                                frontUrl = docs.pan.front;
                            }
                            break;
                        case "drivinglicence":
                            if (docs.drivingLicence != null) {
                                frontUrl = docs.drivingLicence.front;
                                backUrl = docs.drivingLicence.back;
                            }
                            break;
                        case "rc":
                            if (docs.rc != null) {
                                frontUrl = docs.rc.front;
                                backUrl = docs.rc.back;
                            }
                            break;
                    }

                    if (frontUrl != null && !frontUrl.isEmpty()) {
                        binding.imgFrontPreview.setVisibility(View.VISIBLE);

                        Glide.with(binding.getRoot().getContext())
                                .load(frontUrl)
                                .centerCrop()
                                .into(binding.imgFrontPreview);

                    }

                    if (backUrl != null && !backUrl.isEmpty()) {
                        binding.imgBackPreview.setVisibility(View.VISIBLE);
                        Glide.with(binding.getRoot().getContext())
                                .load(frontUrl)
                                .centerCrop()
                                .into(binding.imgBackPreview);

                    }

                    Log.d("DOC_FETCH", "✅ Showing images for " + docType);
                } else {
                    Log.e("GET_FAIL", "❌ Response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DocumentGetResponse> call, Throwable t) {
                Log.e("GET_ERROR", "⚠️ Error: " + t.getMessage());
            }
        });
    }


    private RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private MultipartBody.Part prepareFilePart(String partName, File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private File getFileFromUri(Uri uri) {
        File file = new File(getCacheDir(), System.currentTimeMillis() + "_temp.jpg");
        try (InputStream input = getContentResolver().openInputStream(uri);
             FileOutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = input.read(buffer)) > 0) output.write(buffer, 0, len);
        } catch (Exception e) {
            Log.e("FileError", "getFileFromUri: " + e.getMessage());
        }
        return file;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
