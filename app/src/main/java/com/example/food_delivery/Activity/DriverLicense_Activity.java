package com.example.food_delivery.Activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.food_delivery.Model.DocumentModel;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityDriverLicenseBinding;

import java.util.ArrayList;

public class DriverLicense_Activity extends AppCompatActivity {

    private ActivityDriverLicenseBinding binding;
    private ArrayList<DocumentModel> documentList;
    private Uri frontUri, backUri;
    private Uri tempCameraUri;
    private String currentDoc = "";

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String[]> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverLicenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        documentList = DocumentPrefs.getDocumentList(this);

        binding.ivBack.setOnClickListener(v -> finish());
        initLaunchers();

        binding.btnUploadFrontDL.setOnClickListener(v -> selectImage("dl_front"));
        binding.btnUploadBackDL.setOnClickListener(v -> selectImage("dl_back"));

        binding.btnSubmitDL.setOnClickListener(v -> {
            if (frontUri != null && backUri != null) {
                saveDocument("dl_front", frontUri);
                saveDocument("dl_back", backUri);
                DocumentPrefs.saveDocumentList(this, documentList);
                Toast.makeText(this, "✅ Driving License submitted successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "⚠ Please upload both front and back photos!", Toast.LENGTH_SHORT).show();
            }
        });

        loadSavedImages(); // Load previews on reopen
    }

    private void initLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result && tempCameraUri != null) {
                        if (currentDoc.equals("dl_front")) frontUri = tempCameraUri;
                        else backUri = tempCameraUri;
                        showPreview(currentDoc, tempCameraUri);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        try {
                            final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (Exception ignored) { }
                        if (currentDoc.equals("dl_front")) frontUri = uri;
                        else backUri = uri;
                        showPreview(currentDoc, uri);
                    }
                });
    }

    private void selectImage(String docName) {
        currentDoc = docName;
        String[] options = {"Camera", "Gallery"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                })
                .show();
    }

    private void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
            return;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "DL_Image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp");
        tempCameraUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        cameraLauncher.launch(tempCameraUri);
    }

    private void openGallery() {
        galleryLauncher.launch(new String[]{"image/*"});
    }

    private void saveDocument(String docName, Uri uri) {
        boolean found = false;
        for (DocumentModel model : documentList) {
            if (model.getDocName().equals(docName)) {
                model.setImageUri(uri.toString());
                found = true;
                break;
            }
        }
        if (!found) documentList.add(new DocumentModel(docName, uri.toString()));
    }

    private void showPreview(String docName, Uri uri) {
        if (docName.equals("dl_front")) {
            Glide.with(this).load(uri).into(binding.imgFrontPreviewDL);
            binding.imgFrontPreviewDL.setVisibility(android.view.View.VISIBLE);
        } else if (docName.equals("dl_back")) {
            Glide.with(this).load(uri).into(binding.imgBackPreviewDL);
            binding.imgBackPreviewDL.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void loadSavedImages() {
        for (DocumentModel model : documentList) {
            String uriStr = model.getImageUri();
            if (uriStr == null || uriStr.isEmpty()) continue; // <-- Null-safe check

            Uri uri = Uri.parse(uriStr);
            if (model.getDocName().equals("dl_front")) {
                frontUri = uri;
                Glide.with(this).load(frontUri).into(binding.imgFrontPreviewDL);
                binding.imgFrontPreviewDL.setVisibility(android.view.View.VISIBLE);
            } else if (model.getDocName().equals("dl_back")) {
                backUri = uri;
                Glide.with(this).load(backUri).into(binding.imgBackPreviewDL);
                binding.imgBackPreviewDL.setVisibility(android.view.View.VISIBLE);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
