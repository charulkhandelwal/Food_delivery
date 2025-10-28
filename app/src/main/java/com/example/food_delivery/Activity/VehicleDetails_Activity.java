package com.example.food_delivery.Activity;

import android.app.Activity;
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

import com.example.food_delivery.Model.DocumentModel;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityVehicleDetailsBinding;

import java.util.ArrayList;

public class VehicleDetails_Activity extends AppCompatActivity {

    private ActivityVehicleDetailsBinding binding;
    private ArrayList<DocumentModel> documentList;
    private Uri frontUri, backUri, tempCameraUri;
    private String currentDoc = "";

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String[]> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        documentList = DocumentPrefs.getDocumentList(this);

        initLaunchers();

        // Back button
        binding.ivBack.setOnClickListener(v -> finish());

        binding.btnUploadFront.setOnClickListener(v -> selectImage("vehicle_front"));
        binding.btnUploadBack.setOnClickListener(v -> selectImage("vehicle_back"));


        binding.btnSubmit.setOnClickListener(v -> saveVehicleDetails());

        loadSavedImages();
    }

    private void initLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result && tempCameraUri != null) {
                        if (currentDoc.equals("vehicle_front")) frontUri = tempCameraUri;
                        else backUri = tempCameraUri;
                        showPreview(currentDoc, tempCameraUri);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        try {
                            final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (Exception ignored) {}
                        if (currentDoc.equals("vehicle_front")) frontUri = uri;
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
                }).show();
    }

    private void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
            return;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Vehicle_Image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp");
        tempCameraUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        cameraLauncher.launch(tempCameraUri);
    }

    private void openGallery() {
        galleryLauncher.launch(new String[]{"image/*"});
    }

    private void saveVehicleDetails() {
        if (frontUri == null || backUri == null) {
            Toast.makeText(this, "⚠ Please upload both front and back photos!", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<DocumentModel> docList = DocumentPrefs.getDocumentList(this);


        boolean frontExists = false;
        for (DocumentModel doc : docList) {
            if (doc.getDocName().equals("vehicle_front")) {
                doc.setImageUri(frontUri.toString());
                frontExists = true;
                break;
            }
        }
        if (!frontExists) {
            DocumentModel frontDoc = new DocumentModel("vehicle_front", "Vehicle RC Front");
            frontDoc.setImageUri(frontUri.toString());
            docList.add(frontDoc);
        }


        boolean backExists = false;
        for (DocumentModel doc : docList) {
            if (doc.getDocName().equals("vehicle_back")) {
                doc.setImageUri(backUri.toString());
                backExists = true;
                break;
            }
        }
        if (!backExists) {
            DocumentModel backDoc = new DocumentModel("vehicle_back", "Vehicle RC Back");
            backDoc.setImageUri(backUri.toString());
            docList.add(backDoc);
        }

        DocumentPrefs.saveDocumentList(this, docList);

        Toast.makeText(this, "Vehicle details saved!", Toast.LENGTH_SHORT).show();

        Intent result = new Intent();
        result.putExtra("doc_type", "vehicle");
        setResult(Activity.RESULT_OK, result);
        finish();

    }

    private void showPreview(String docName, Uri uri) {
        if (docName.equals("vehicle_front")) {
            binding.imgFrontPreview.setImageURI(uri);
            binding.imgFrontPreview.setVisibility(android.view.View.VISIBLE);
        } else if (docName.equals("vehicle_back")) {
            binding.imgBackPreview.setImageURI(uri);
            binding.imgBackPreview.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void loadSavedImages() {
        for (DocumentModel doc : documentList) {
            Uri uri = Uri.parse(doc.getImageUri());
            if (doc.getDocName().equals("vehicle_front")) {
                frontUri = uri;
                binding.imgFrontPreview.setImageURI(frontUri);
                binding.imgFrontPreview.setVisibility(android.view.View.VISIBLE);
            } else if (doc.getDocName().equals("vehicle_back")) {
                backUri = uri;
                binding.imgBackPreview.setImageURI(backUri);
                binding.imgBackPreview.setVisibility(android.view.View.VISIBLE);
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
