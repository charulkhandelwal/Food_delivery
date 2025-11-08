package com.example.food_delivery.Activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.food_delivery.Adapter.WalletHistoryAdapter;
import com.example.food_delivery.Model.WalletHistoryModel;
import com.example.food_delivery.databinding.ActivityWalletHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class WalletHistory extends AppCompatActivity {

    private ActivityWalletHistoryBinding binding;
    private WalletHistoryAdapter adapter;
    private final List<WalletHistoryModel> walletList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.recyclerWalletHistory.setLayoutManager(new LinearLayoutManager(this));


        walletList.add(new WalletHistoryModel(
                " Dr.Charul khandelwal", "Sector 45, Gurugram, Haryana",
                "khandelwal dhaba",
                "2x Burger, 1x Fries, 1x Coke",
                "Delivered on: 04 Nov 2025, 08:30 PM",
                "UPI Payment", "TXN12345ABCDE", "₹420"
        ));

        walletList.add(new WalletHistoryModel(
                "Anjali Singh", "MG Road, Delhi",
                "khandelwal dhaba",
                "1x Pizza, 2x Coke",
                "Delivered on: 02 Nov 2025, 09:10 PM",
                "Cash on Delivery", "TXN98765FGHIJ", "₹580"
        ));

        walletList.add(new WalletHistoryModel(
                "Kunal Verma", "DLF Phase 2, Gurugram",
                "khandelwal dhaba",
                "1x Sandwich, 1x Cold Coffee",
                "Delivered on: 01 Nov 2025, 01:15 PM",
                "Card Payment", "TXN55678KLMNO", "₹310"
        ));



        adapter = new WalletHistoryAdapter(walletList);
        binding.recyclerWalletHistory.setAdapter(adapter);
    }
}
