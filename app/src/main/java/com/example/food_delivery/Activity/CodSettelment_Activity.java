package com.example.food_delivery.Activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_delivery.Adapter.CodSettelmentAdapter;
import com.example.food_delivery.Model.CodSettelmentModel;
import com.example.food_delivery.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodSettelment_Activity extends AppCompatActivity {

    private RecyclerView recyclerCodSettlement;
    private TextView tvTotalPayment, tvCommission, tvToPayCalculation, tvFinalAmount;
    private MaterialButton btnPay;

    private List<CodSettelmentModel> codList;
    private CodSettelmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cod_settelment);

        initViews();
        setupRecycler();
        loadDummyData(); // TODO: Replace with API data
        calculateSummary();
        setupPayButton();
    }

    // ✅ Initialize all views
    private void initViews() {
        recyclerCodSettlement = findViewById(R.id.recyclerCodSettlement);
        tvTotalPayment = findViewById(R.id.tvTotalPayment);
        tvCommission = findViewById(R.id.tvCommission);
        tvToPayCalculation = findViewById(R.id.tvToPayCalculation);
        tvFinalAmount = findViewById(R.id.tvFinalAmount);
        btnPay = findViewById(R.id.btnPay);
    }

    // ✅ Setup RecyclerView
    private void setupRecycler() {
        recyclerCodSettlement.setLayoutManager(new LinearLayoutManager(this));
        codList = new ArrayList<>();
        adapter = new CodSettelmentAdapter(this, codList);
        recyclerCodSettlement.setAdapter(adapter);
    }

    // ✅ Dummy Data (Replace with API Response)
    private void loadDummyData() {
        codList.add(new CodSettelmentModel(
                "Pizza Hub", "12345", "COD", 500, 50,
                Arrays.asList("Margherita", "Garlic Bread", "Pepsi")
        ));

        codList.add(new CodSettelmentModel(
                "Burger Point", "67890", "COD", 400, 40,
                Arrays.asList("Cheese Burger", "French Fries", "Coke")
        ));

        codList.add(new CodSettelmentModel(
                "Cafe Mocha", "13579", "COD", 600, 60,
                Arrays.asList("Cold Coffee", "Brownie")
        ));
        codList.add(new CodSettelmentModel(
                "Cafe Mocha", "13579", "COD", 600, 60,
                Arrays.asList("Cold Coffee", "Brownie")
        ));
        codList.add(new CodSettelmentModel(
                "Cafe Mocha", "13579", "COD", 600, 60,
                Arrays.asList("Cold Coffee", "Brownie")
        ));

        adapter.notifyDataSetChanged();
    }

    // ✅ Calculate Total, Commission & Final Amount
    private void calculateSummary() {
        double totalPayment = 0;
        double totalCommission = 0;

        for (CodSettelmentModel model : codList) {
            totalPayment += model.getTotalAmount();
            totalCommission += model.getCommission();
        }

        double toPay = totalPayment - totalCommission;

        // ✅ Set TextViews
        tvTotalPayment.setText("Total Payment: ₹" + totalPayment);
        tvCommission.setText("Commission: ₹" + totalCommission);
        tvToPayCalculation.setText("₹" + totalPayment + " - ₹" + totalCommission);
        tvFinalAmount.setText("Final Amount: ₹" + toPay);
    }

    // ✅ Pay Button Action
    private void setupPayButton() {
        btnPay.setOnClickListener(v -> {
            Toast.makeText(this, "Payment of ₹" + tvFinalAmount.getText().toString().replace("Final Amount: ₹", "") + " Successful ✅", Toast.LENGTH_SHORT).show();
        });
    }
}
