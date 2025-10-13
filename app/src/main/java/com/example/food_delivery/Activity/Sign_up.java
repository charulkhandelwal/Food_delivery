package com.example.food_delivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.OtpResponse;
import com.example.food_delivery.databinding.ActivitySignUpBinding;

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
    String countryCode = "+91"; // Default country code

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


        // 🔹 Button click for sending OTP
        binding.btnsendotp.setOnClickListener(view -> {
            phone = binding.etphone.getText() != null ?
                    binding.etphone.getText().toString().trim() : "";

            // ✅ Validation
            if (TextUtils.isEmpty(phone)) {
                binding.etphone.setError("Please enter phone number");
                binding.etphone.requestFocus();
                return;
            }

            // If number starts with +91, clean it up
            if (phone.startsWith("+91")) {
                phone = phone.replace("+91", "").trim();
            }

            // Validate 10 digit mobile number
            if (phone.length() != 10 || !phone.matches("\\d{10}")) {
                binding.etphone.setError("Enter valid 10 digit mobile number");
                binding.etphone.requestFocus();
                return;
            }

            if (!binding.checkboxterms.isChecked()) {
                Toast.makeText(Sign_up.this,
                        "Please accept Terms & Privacy Policy", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Proceed to send OTP
            sendOtpApi(countryCode, phone);
        });
    }

    private void sendOtpApi(String countryCode, String phone) {
        OtpApi api = ApiClient.getClient().create(OtpApi.class);

        // ✅ Prepare request body
        Map<String, String> body = new HashMap<>();
        body.put("countryCode", countryCode);
        body.put("mobile", phone);
        body.put("countryCode", "+91");

        Log.e("SEND_OTP_API", "📤 Sending → " + body);

        Call<OtpResponse> call = api.sendOtp(body);
        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OtpResponse otpResponse = response.body();

                    if (otpResponse.success) {
                        Log.e("OTP_SUCCESS", "✅ " + otpResponse.message);

                        Toast.makeText(Sign_up.this, otpResponse.message, Toast.LENGTH_SHORT).show();

                        // ✅ Move to OTP screen and pass both values
                        Intent intent = new Intent(Sign_up.this, OtpActivity.class);
                        intent.putExtra("mobile", phone);
                        intent.putExtra("countryCode", countryCode);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Sign_up.this, otpResponse.message, Toast.LENGTH_SHORT).show();
                        Log.e("OTP_FAILED", otpResponse.message);
                    }
                } else {
                    Toast.makeText(Sign_up.this, "Unexpected server response", Toast.LENGTH_SHORT).show();
                    Log.e("OTP_ERROR", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                Log.e("OTP_ERROR", "❌ onFailure: " + t.getMessage(), t);
                Toast.makeText(Sign_up.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
