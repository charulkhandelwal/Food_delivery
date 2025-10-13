package com.example.food_delivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.OtpResponse;
import com.example.food_delivery.databinding.ActivitySignUpBinding;
import com.google.android.material.snackbar.Snackbar;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Sign_up extends AppCompatActivity {

    ActivitySignUpBinding binding;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        new Thread(() -> {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("164.52.197.192", 5678), 5000);
                Log.e("TEST", "✅ Connected successfully!");
            } catch (Exception e) {
                Log.e("TEST", "❌ Connection failed: " + e.getMessage());
            }
        }).start();

        binding.btnsendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = binding.etphone.getText() != null ?
                        binding.etphone.getText().toString().trim() : "";

                // ✅ Validate phone number
                if (TextUtils.isEmpty(phone)) {
                    binding.etphone.setError("Please enter phone number");
                    binding.etphone.requestFocus();
                    return;
                } else if (phone.length() != 10 || !phone.matches("\\d{10}")) {
                    binding.etphone.setError("Enter valid 10 digit mobile number");
                    binding.etphone.requestFocus();
                    return;
                } else if (!binding.checkboxterms.isChecked()) {
                    Toast.makeText(Sign_up.this,
                            "Please accept Terms & Privacy Policy", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    callApi();
                }


            }
        });
    }

    private void callApi() {

        OtpApi api = ApiClient.getClient().create(OtpApi.class);

        Map<String, String> body = new HashMap<>();
        body.put("mobile", phone);

        Call<OtpResponse> call = api.sendOtp(body);
        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OtpResponse otpResponse = response.body();
                    if (otpResponse.success) {
                        Intent intent = new Intent(Sign_up.this, OtpActivity.class);
                        intent.putExtra("mobile", phone);
                        startActivity(intent);
                        Log.e("login successfully", "tag");
                        Toast.makeText(Sign_up.this,  otpResponse.message, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(Sign_up.this, otpResponse.message, Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                Log.e("login error", t.getMessage(), t);
                Toast.makeText(Sign_up.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }
}
