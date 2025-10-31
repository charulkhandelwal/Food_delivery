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
import com.example.food_delivery.Model.DocumentGetResponse;
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
    private String countryCode = "+91";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        phone = getIntent().getStringExtra("mobile");
        String codeFromIntent = getIntent().getStringExtra("countryCode");
        if (codeFromIntent != null && !codeFromIntent.isEmpty()) {
            countryCode = codeFromIntent;
        }

        Log.e("OTP_ACTIVITY", "📞 Phone: " + phone + " | CountryCode: " + countryCode);

        binding.BtnBack.setOnClickListener(v -> onBackPressed());
        setupOtpInputs();

        binding.btnverify.setOnClickListener(v -> {
            String otp = getOtpFromInputs();
            if (otp.length() != 6) {
                Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
            } else {
                verifyOtpApi(phone, otp, countryCode);
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
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpInputs.length - 1) {
                        otpInputs[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpInputs[index - 1].requestFocus();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
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
                        boolean isNewUser = otpResponse.results.isNewUser;

                        DocumentPrefs.saveToken(OtpActivity.this, token);
                        DocumentPrefs.savePartnerId(OtpActivity.this, partnerId);

                        Log.e("TOKEN_SAVED", token);
                        Log.e("PARTNER_ID_SAVED", partnerId);
                        Log.e("IS_NEW_USER", "isNewUser: " + isNewUser);

                        connectSocket(partnerId);

                        if (isNewUser) {
                            // New user → go fill personal info first
                            Log.d("NAVIGATION", "🆕 New user → Personal Info screen");
                            startActivity(new Intent(OtpActivity.this, Personal_informationActivity.class));
                            finish();
                        } else {
                            // Existing user → fetch documents
                            Log.d("NAVIGATION", "👤 Existing user → checking document statuses...");
                            getDocumentsFromServer(token);
                        }

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

    private void getDocumentsFromServer(String token) {
        OtpApi api = ApiClient.getClientWithToken(token).create(OtpApi.class);
        Call<DocumentGetResponse> call = api.getdocuments();

        Log.d("DOC_API", "📡 Fetching documents with token: " + token);

        call.enqueue(new Callback<DocumentGetResponse>() {
            @Override
            public void onResponse(Call<DocumentGetResponse> call, Response<DocumentGetResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    DocumentGetResponse.Documents docs = response.body().getResults().documents;

                    String aadharStatus = docs.aadhar != null ? docs.aadhar.status : "pending";
                    String panStatus = docs.pan != null ? docs.pan.status : "pending";
                    String dlStatus = docs.drivingLicence != null ? docs.drivingLicence.status : "pending";
                    String rcStatus = docs.rc != null ? docs.rc.status : "pending";
                    String bankStatus = docs.bankAccountDetails != null ? docs.bankAccountDetails.status : "pending";

                    Log.e("DOC_STATUS",
                            "📄 Aadhar=" + aadharStatus +
                                    ", PAN=" + panStatus +
                                    ", DL=" + dlStatus +
                                    ", RC=" + rcStatus +
                                    ", Bank=" + bankStatus);

                    boolean allApproved =
                            aadharStatus.equalsIgnoreCase("approved") &&
                                    panStatus.equalsIgnoreCase("approved") &&
                                    dlStatus.equalsIgnoreCase("approved") &&
                                    rcStatus.equalsIgnoreCase("approved") &&
                                    bankStatus.equalsIgnoreCase("approved");

                    Intent intent;
                    if (allApproved) {
                        Log.d("NAVIGATION", "✅ All documents approved → going to MAIN");
                        intent = new Intent(OtpActivity.this, MainActivity.class);
                    } else {
                        Log.d("NAVIGATION", "📄 Pending or rejected docs → going to DOCUMENT screen");
                        intent = new Intent(OtpActivity.this, Document_Activity.class);
                    }

                    startActivity(intent);
                    finish();

                } else {
                    Log.e("DOC_API_FAIL", "❌ Response code: " + response.code());
                    Toast.makeText(OtpActivity.this, "Document fetch failed", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OtpActivity.this, Document_Activity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<DocumentGetResponse> call, Throwable t) {
                Log.e("DOC_API_ERROR", "⚠️ Failed to fetch documents: " + t.getMessage());
                startActivity(new Intent(OtpActivity.this, Document_Activity.class));
                finish();
            }
        });
    }

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
