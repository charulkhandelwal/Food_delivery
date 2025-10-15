package com.example.food_delivery.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Model.DocumentModel;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityPersonalDocumentBinding;

import java.util.ArrayList;

public class personal_Document extends AppCompatActivity {

    private ActivityPersonalDocumentBinding binding;
    private ArrayList<DocumentModel> documentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        documentList = DocumentPrefs.getDocumentList(this);

        binding.ivBack.setOnClickListener(v -> finish());

        // Navigation to document upload screens
        binding.cardAadhaar.setOnClickListener(v -> startActivity(new Intent(this, Aadharcard_Activity.class)));
        binding.cardPan.setOnClickListener(v -> startActivity(new Intent(this, PANCARD_Activity.class)));
        binding.cardDL.setOnClickListener(v -> startActivity(new Intent(this, DriverLicense_Activity.class)));

        binding.btnSubmit.setOnClickListener(v -> {
            boolean hasAadhaar = false, hasPan = false, hasDl = false;
            for (DocumentModel model : documentList) {
                String name = model.getDocName().toLowerCase();
                if (name.contains("aadhaar")) hasAadhaar = true;
                if (name.contains("pan")) hasPan = true;
                if (name.contains("dl")) hasDl = true;
            }

            if (hasAadhaar && hasPan && hasDl) {
                // ✅ Add a single "aadhaar", "pan", and "dl" marker entries
                updateDocumentStatus("aadhaar");
                updateDocumentStatus("pan");
                updateDocumentStatus("dl");

                Toast.makeText(this, "✅ All documents uploaded!", Toast.LENGTH_SHORT).show();
                binding.pendingSection.setVisibility(View.GONE);
                binding.completedSection.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "⚠ Please upload Aadhaar, PAN & Driving License!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDocumentStatus(String type) {
        boolean exists = false;
        for (DocumentModel model : documentList) {
            if (model.getDocName().equals(type)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            DocumentModel doc = new DocumentModel(type, type.toUpperCase());
            doc.setImageUri("saved"); // just a placeholder marker
            documentList.add(doc);
        }
        DocumentPrefs.saveDocumentList(this, documentList);
    }

    @Override
    protected void onResume() {
        super.onResume();

        documentList = DocumentPrefs.getDocumentList(this);

        for (DocumentModel model : documentList) {
            if (model.getImageUri() == null) continue;
            Uri uri = Uri.parse(model.getImageUri());
            String name = model.getDocName().toLowerCase();
            if (name.contains("aadhaar")) {
                binding.ivAadhaarCheck.setImageResource(android.R.drawable.checkbox_on_background);
            } else if (name.contains("pan")) {
                binding.ivPanCheck.setImageResource(android.R.drawable.checkbox_on_background);
            } else if (name.contains("dl")) {
                binding.ivDLCheck.setImageResource(android.R.drawable.checkbox_on_background);
            }
        }
    }
}
