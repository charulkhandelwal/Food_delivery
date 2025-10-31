package com.example.food_delivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.DocumentGetResponse;
import com.example.food_delivery.Model.DocumentModel;
import com.example.food_delivery.R;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityPersonalDocumentBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class personal_Document extends AppCompatActivity {

    private ActivityPersonalDocumentBinding binding;
    private ArrayList<DocumentModel> documentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        documentList = DocumentPrefs.getDocumentList(this);
        setupClicks();
        getDocumentsFromServer();
    }

    private void setupClicks() {
        binding.ivBack.setOnClickListener(v -> finish());
        binding.cardAadhaar.setOnClickListener(v -> startActivity(new Intent(this, Aadharcard_Activity.class)));
        binding.cardPan.setOnClickListener(v -> startActivity(new Intent(this, PANCARD_Activity.class)));
        binding.cardDL.setOnClickListener(v -> startActivity(new Intent(this, DriverLicense_Activity.class)));
    }

    private void getDocumentsFromServer() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String token = DocumentPrefs.getToken(this);
        ArrayList<DocumentModel> localDocs = DocumentPrefs.getDocumentList(this);

        if (token == null || token.isEmpty()) {
            binding.progressBar.setVisibility(View.GONE);
            documentList = localDocs;
            updateUIStatus();
            return;
        }

        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        Call<DocumentGetResponse> call = api.getdocuments();

        call.enqueue(new Callback<DocumentGetResponse>() {
            @Override
            public void onResponse(Call<DocumentGetResponse> call, Response<DocumentGetResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<DocumentModel> serverDocs = response.body().getDocuments();
                    if (serverDocs != null) {
                        for (DocumentModel serverDoc : serverDocs) {
                            for (DocumentModel localDoc : localDocs) {
                                if (serverDoc.getDocName() != null && localDoc.getDocName() != null &&
                                        serverDoc.getDocName().equalsIgnoreCase(localDoc.getDocName()) &&
                                        localDoc.getImageUri() != null && !localDoc.getImageUri().isEmpty()) {
                                    serverDoc.setImageUri(localDoc.getImageUri());
                                }
                            }
                            DocumentPrefs.updateSingleDocument(personal_Document.this, serverDoc);
                        }
                        documentList = new ArrayList<>(serverDocs);
                    }
                } else {
                    documentList = localDocs;
                }
                updateUIStatus();
            }

            @Override
            public void onFailure(Call<DocumentGetResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                documentList = localDocs;
                updateUIStatus();
            }
        });
    }

    private void updateUIStatus() {
        boolean hasAadhaar = false, hasPan = false, hasDl = false;

        for (DocumentModel model : documentList) {
            String name = model.getDocName() == null ? "" : model.getDocName().toLowerCase();
            String imageUri = model.getImageUri();
            boolean verified = model.isVerified();

            if (name.contains("aadhaar") || name.contains("aadhar")) {
                hasAadhaar = true;
                setImageCheck(binding.ivAadhaarCheck, imageUri);
                binding.txtAadharStatus.setText(getStatusText("Aadhaar", verified, imageUri));
            } else if (name.contains("pan")) {
                hasPan = true;
                setImageCheck(binding.ivPanCheck, imageUri);
                binding.txtPanStatus.setText(getStatusText("PAN", verified, imageUri));
            } else if (name.contains("dl") || name.contains("driving")) {
                hasDl = true;
                setImageCheck(binding.ivDLCheck, imageUri);
                binding.txtDLStatus.setText(getStatusText("Driving License", verified, imageUri));
            }
        }

        if (!hasAadhaar) binding.txtAadharStatus.setText("Aadhaar not uploaded ❌");
        if (!hasPan) binding.txtPanStatus.setText("PAN not uploaded ❌");
        if (!hasDl) binding.txtDLStatus.setText("Driving License not uploaded ❌");
    }

    private String getStatusText(String docName, boolean verified, String imageUri) {
        if (imageUri == null || imageUri.isEmpty()) return docName + " not uploaded ❌";
        else if (!verified) return docName + " pending ⏳";
        else return docName + " verified ✅";
    }

    private void setImageCheck(ImageView view, String imageUri) {
        view.setVisibility(View.VISIBLE);
        if (imageUri != null && !imageUri.isEmpty())
            view.setImageResource(R.drawable.check);
        else
            view.setImageResource(R.drawable.logout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<DocumentModel> localDocs = DocumentPrefs.getDocumentList(this);
        if (documentList == null) documentList = new ArrayList<>();
        for (DocumentModel localDoc : localDocs) {
            boolean found = false;
            for (DocumentModel doc : documentList) {
                if (doc.getDocName() != null && doc.getDocName().equalsIgnoreCase(localDoc.getDocName())) {
                    if (localDoc.getImageUri() != null && !localDoc.getImageUri().isEmpty()) {
                        doc.setImageUri(localDoc.getImageUri());
                    }
                    found = true;
                    break;
                }
            }
            if (!found) documentList.add(localDoc);
        }
        updateUIStatus();
    }
}
