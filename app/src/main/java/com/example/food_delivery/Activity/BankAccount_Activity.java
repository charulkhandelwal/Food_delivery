package com.example.food_delivery.Activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Model.DocumentModel;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityBankAccountBinding;

import java.util.ArrayList;

public class BankAccount_Activity extends AppCompatActivity {

    private ActivityBankAccountBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBankAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("BankPrefs", MODE_PRIVATE);
        loadSavedData();

        binding.btnSubmitBankDetails.setOnClickListener(v -> saveBankDetails());
    }

    private void saveBankDetails() {
        String holderName = binding.etAccountHolderName.getText().toString().trim();
        String accountNumber = binding.etAccountNumber.getText().toString().trim();
        String ifscCode = binding.etIfscCode.getText().toString().trim();

        if (holderName.isEmpty() || accountNumber.isEmpty() || ifscCode.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("holderName", holderName);
        editor.putString("accountNumber", accountNumber);
        editor.putString("ifscCode", ifscCode);
        editor.apply();


        ArrayList<DocumentModel> docList = DocumentPrefs.getDocumentList(this);
        boolean bankAdded = false;
        for (DocumentModel doc : docList) {
            if (doc.getDocName().equals("bank")) {
                bankAdded = true;
                break;
            }
        }
        if (!bankAdded) {
            docList.add(new DocumentModel("bank", "Bank Account Details"));
            DocumentPrefs.saveDocumentList(this, docList);
        }

        Toast.makeText(this, "Bank details saved!", Toast.LENGTH_SHORT).show();


        setResult(Activity.RESULT_OK);
        finish();
    }

    private void loadSavedData() {
        String holderName = sharedPreferences.getString("holderName", null);
        String accountNumber = sharedPreferences.getString("accountNumber", null);
        String ifscCode = sharedPreferences.getString("ifscCode", null);

        if (holderName != null) binding.etAccountHolderName.setText(holderName);
        if (accountNumber != null) binding.etAccountNumber.setText(accountNumber);
        if (ifscCode != null) binding.etIfscCode.setText(ifscCode);
    }
}
