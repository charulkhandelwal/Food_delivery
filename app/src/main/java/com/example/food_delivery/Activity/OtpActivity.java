package com.example.food_delivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.OtpVerifyResponse;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.Socket.SocketManager;
import com.example.food_delivery.databinding.ActivityOtpBinding;

import java.util.HashMap;
import java.util.Map;

import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    private ActivityOtpBinding binding;
    private String phone;
    private String countryCode = "+91"; // default value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 🔹 Get phone number and country code from intent
        phone = getIntent().getStringExtra("mobile");
        String codeFromIntent = getIntent().getStringExtra("countryCode");

        if (codeFromIntent != null && !codeFromIntent.isEmpty()) {
            countryCode = codeFromIntent;
        }

        Log.e("OTP_ACTIVITY", "📞 Phone: " + phone + " | CountryCode: " + countryCode);

        // 🔹 Back button
        binding.BtnBack.setOnClickListener(v -> onBackPressed());

        // 🔹 Setup OTP inputs
        setupOtpInputs();

        // 🔹 Verify OTP button click
        binding.btnverify.setOnClickListener(v -> {
            String otp = getOtpFromInputs();
            if (otp.length() != 6) {
                Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
            } else {
                verifyOtpApi(phone, otp, countryCode);
            }
        });
    }

    // 🔹 Handles auto movement between OTP boxes
    private void setupOtpInputs() {
        EditText[] otpInputs = {
                binding.etOtp1, binding.etOtp2, binding.etOtp3,
                binding.etOtp4, binding.etOtp5, binding.etOtp6
        };

        for (int i = 0; i < otpInputs.length; i++) {
            int index = i;
            otpInputs[index].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpInputs.length - 1) {
                        otpInputs[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpInputs[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    // 🔹 Collects all 6 digits of the OTP
    private String getOtpFromInputs() {
        return binding.etOtp1.getText().toString().trim() +
                binding.etOtp2.getText().toString().trim() +
                binding.etOtp3.getText().toString().trim() +
                binding.etOtp4.getText().toString().trim() +
                binding.etOtp5.getText().toString().trim() +
                binding.etOtp6.getText().toString().trim();
    }

    // 🔹 API Call for OTP Verification
    private void verifyOtpApi(String phone, String otp, String countryCode) {
        OtpApi api = ApiClient.getClient().create(OtpApi.class);

        Map<String, String> body = new HashMap<>();
        body.put("mobile", phone);
        body.put("otp", otp);
        body.put("countryCode", countryCode);

        Log.e("VERIFY_OTP_API", "📤 Sending → " + body);

        Call<OtpVerifyResponse> call = api.verifyOtp(body);
        call.enqueue(new Callback<OtpVerifyResponse>() {
            @Override
            public void onResponse(Call<OtpVerifyResponse> call, Response<OtpVerifyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OtpVerifyResponse otpResponse = response.body();

                    if (otpResponse.success) {
                        Log.d("OTP_VERIFY", "✅ OTP Verified Successfully");

                        String token = otpResponse.results.token;
                        String partnerId = otpResponse.results.partner.id;

                        // 🔹 Save token & partnerId
                        DocumentPrefs.saveToken(OtpActivity.this, token);
                        DocumentPrefs.savePartnerId(OtpActivity.this, partnerId);

                        Log.e("TOKEN_SAVED", token);
                        Log.e("PARTNER_ID_SAVED", partnerId);

                        // 🔹 Connect Socket and Emit Event
                        connectSocket(partnerId);

                        Toast.makeText(OtpActivity.this, "OTP Verified Successfully", Toast.LENGTH_SHORT).show();

                        // 🔹 Go to next screen
                        Intent intent = new Intent(OtpActivity.this, Personal_informationActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(OtpActivity.this, otpResponse.message, Toast.LENGTH_SHORT).show();
                        Log.e("OTP_VERIFY_FAIL", otpResponse.message);
                    }
                } else {
                    Toast.makeText(OtpActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("OTP_VERIFY_ERROR", "Response Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<OtpVerifyResponse> call, Throwable t) {
                Toast.makeText(OtpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OTP_VERIFY_FAILURE", "Error: " + t.getMessage(), t);
            }
        });
    }

    // 🔹 Handles socket connection and partner_online emission
    private void connectSocket(String partnerId) {
        try {
            SocketManager socketManager = SocketManager.getInstance();
            Socket socket = socketManager.getSocket();

            socket.on(Socket.EVENT_CONNECT, args -> {
                Log.d("SOCKET", "✅ Connected to server");
                socket.emit("partner_online", partnerId);
                Log.d("SOCKET", "📡 partner_online → " + partnerId);
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                Log.e("SOCKET", "❌ Connect error: " + args[0]);
            });

            socketManager.connect();
        } catch (Exception e) {
            Log.e("SOCKET_ERROR", "⚠️ Error connecting socket: " + e.getMessage(), e);
        }
    }
}
