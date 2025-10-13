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


        binding.cardAadhaar.setOnClickListener(v -> startActivity(new Intent(this, Aadharcard_Activity.class)));
        binding.cardPan.setOnClickListener(v -> startActivity(new Intent(this, PANCARD_Activity.class)));
        binding.cardDL.setOnClickListener(v -> startActivity(new Intent(this, DriverLicense_Activity.class)));


        binding.btnSubmit.setOnClickListener(v -> {
            boolean hasAadhaar = false, hasPan = false, hasDl = false;
            for (DocumentModel model : documentList) {
                if (model.getDocName().contains("aadhaar")) hasAadhaar = true;
                if (model.getDocName().contains("pan")) hasPan = true;
                if (model.getDocName().contains("dl")) hasDl = true;
            }

            if (hasAadhaar && hasPan && hasDl) {
                Toast.makeText(this, "✅ All documents uploaded!", Toast.LENGTH_SHORT).show();
                binding.pendingSection.setVisibility(View.VISIBLE);
                binding.completedSection.setVisibility(android.view.View.VISIBLE);
            } else {
                Toast.makeText(this, "⚠ Please upload Aadhaar, PAN & Driving License!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        documentList = DocumentPrefs.getDocumentList(this);

        for (DocumentModel model : documentList) {
            if (model.getImageUri() == null) continue;
            Uri uri = Uri.parse(model.getImageUri());
            switch (model.getDocName()) {
                case "aadhaar_front":
                case "aadhaar_back":
                    binding.ivAadhaarCheck.setImageResource(android.R.drawable.checkbox_on_background);
                    break;
                case "pan_front":
                case "pan_back":
                    binding.ivPanCheck.setImageResource(android.R.drawable.checkbox_on_background);
                    break;
                case "dl_front":
                case "dl_back":
                    binding.ivDLCheck.setImageResource(android.R.drawable.checkbox_on_background);
                    break;
            }
        }
    }
}
