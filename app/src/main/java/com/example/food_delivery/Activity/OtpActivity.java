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
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    ActivityOtpBinding binding;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        phone = getIntent().getStringExtra("mobile");
        Log.e("phone no", phone);

        binding.BtnBack.setOnClickListener(view -> onBackPressed());

        setupOtpInputs();

        binding.btnverify.setOnClickListener(v -> {
            String otp = getOtpFromInputs();
            if (otp.length() != 6) {
                Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
            } else {
                verifyOtpApi(phone, otp);
            }
        });
    }

    private void setupOtpInputs() {
        EditText[] otpInputs = {
                binding.etOtp1, binding.etOtp2, binding.etOtp3,
                binding.etOtp4, binding.etOtp5, binding.etOtp6
        };

        for (int i = 0; i < otpInputs.length; i++) {
            int index = i;
            otpInputs[index].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpInputs.length - 1) {
                        otpInputs[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpInputs[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }
    }

    private String getOtpFromInputs() {
        return binding.etOtp1.getText().toString().trim() +
                binding.etOtp2.getText().toString().trim() +
                binding.etOtp3.getText().toString().trim() +
                binding.etOtp4.getText().toString().trim() +
                binding.etOtp5.getText().toString().trim() +
                binding.etOtp6.getText().toString().trim();
    }

    private void verifyOtpApi(String phone, String otp) {
        OtpApi api = ApiClient.getClient().create(OtpApi.class);

        Map<String, String> body = new HashMap<>();
        body.put("mobile", phone);
        body.put("otp", otp);

        Call<OtpVerifyResponse> call = api.verifyOtp(body);
        call.enqueue(new Callback<OtpVerifyResponse>() {
            @Override
            public void onResponse(Call<OtpVerifyResponse> call, Response<OtpVerifyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OtpVerifyResponse otpResponse = response.body();

                    if (otpResponse.success) {

                        String token = otpResponse.results.token;
                        String partnerId = otpResponse.results.partner.id;

                        DocumentPrefs.saveToken(OtpActivity.this, token);
                        DocumentPrefs.savePartnerId(OtpActivity.this, partnerId);

                        Log.e("Token Saved ✅", token);
                        Log.e("Partner ID Saved ✅", partnerId);


                        SocketManager socketManager = SocketManager.getInstance();
                        Socket socket = socketManager.getSocket();

                        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                try {
                                    Log.d("Socket", "✅ Connected");


                                    socket.emit("partner_online", partnerId);
                                    Log.d("Socket", "📡 Sent event: partner_online → " + partnerId);

                                } catch (Exception e) {
                                    Log.e("Socket", "❌ Error sending partner_online: " + e.getMessage());
                                }
                            }
                        });

                        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                            Log.e("Socket", "❌ Connect error: " + args[0]);
                        });

                        socketManager.connect();

                        Toast.makeText(OtpActivity.this, "OTP Verified Successfully", Toast.LENGTH_SHORT).show();


                        Intent intent = new Intent(OtpActivity.this, Personal_informationActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(OtpActivity.this, otpResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(OtpActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OtpVerifyResponse> call, Throwable t) {
                Toast.makeText(OtpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
