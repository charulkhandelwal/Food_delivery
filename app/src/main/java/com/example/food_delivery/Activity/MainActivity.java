package com.example.food_delivery.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.food_delivery.R;
import com.example.food_delivery.fragment.Account;
import com.example.food_delivery.fragment.Order;

public class MainActivity extends AppCompatActivity {

    Button btnOrders, btnAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOrders = findViewById(R.id.btnOrders);
        btnAccount = findViewById(R.id.btnAccount);


        loadFragment(new Order());
        highlightSelected(btnOrders, btnAccount);

        btnOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new Order());
                highlightSelected(btnOrders, btnAccount);
            }
        });

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new Account());
                highlightSelected(btnAccount, btnOrders);
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }


    private void highlightSelected(Button selected, Button unselected) {
        selected.setBackgroundResource(R.drawable.bottom_selected);
        selected.setTextColor(getResources().getColor(android.R.color.white));

        unselected.setBackgroundResource(R.drawable.bottom_unselected);
        unselected.setTextColor(getResources().getColor(android.R.color.black));
    }
    @Override
    public void onBackPressed() {
        // Close the app instead of going back to login/signup
        super.onBackPressed();
        finishAffinity();
    }

}
