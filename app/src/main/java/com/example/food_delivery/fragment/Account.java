package com.example.food_delivery.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.food_delivery.Activity.AboutActivity;
import com.example.food_delivery.Activity.CodSettelment_Activity;
import com.example.food_delivery.Activity.EditProfileActivity;
import com.example.food_delivery.Activity.FAQ_Activity;
import com.example.food_delivery.Activity.HelpSupportActivity;
import com.example.food_delivery.Activity.OrderHistoryAcitivity;
import com.example.food_delivery.Activity.Refer_win;
import com.example.food_delivery.Activity.Sign_up;
import com.example.food_delivery.Activity.WalletHistory;
import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.GetProfileResponse;
import com.example.food_delivery.R;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.FragmentAccountBinding;
import com.google.gson.Gson;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Account extends Fragment {

    private FragmentAccountBinding binding;
    private OtpApi apiService;
    private static final String TAG = "AccountFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        setupClicks();


        getProfileData();

        return view;
    }

    private void setupClicks() {
      //  binding.icback1.setOnClickListener(v ->
                //startActivity(new Intent(getActivity(), Refer_win.class)));
        binding.backwallethistory.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), WalletHistory.class)));


        binding.backAbout.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AboutActivity.class)));

        binding.backHelp.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), HelpSupportActivity.class)));

        binding.backFaq.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), FAQ_Activity.class)));

        binding.editprofile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));

        binding.backorderhistory.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), OrderHistoryAcitivity.class)));

        binding.backcodsettelment.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CodSettelment_Activity.class)));

        binding.rowLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void getProfileData() {
        String token = DocumentPrefs.getToken(requireContext());
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No token found in SharedPreferences");
            return;
        }

        apiService = ApiClient.getClientWithToken(token).create(OtpApi.class);

        Log.d(TAG, "Calling GET /delivery-partner/profile API...");
        Call<GetProfileResponse> call = apiService.getProfile();

        call.enqueue(new Callback<GetProfileResponse>() {
            @Override
            public void onResponse(Call<GetProfileResponse> call, Response<GetProfileResponse> response) {
                Log.d(TAG, "API Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    GetProfileResponse data = response.body();
                    Log.d(TAG, "Full API Response: " + new Gson().toJson(data));

                    if (data.results != null) {
                        updateUI(data.results);
                    } else {
                        Toast.makeText(requireContext(), "No profile data found", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "data.results is null");
                    }
                } else {
                    Toast.makeText(requireContext(), "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response error body: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GetProfileResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    private void updateUI(GetProfileResponse.Results results) {
        String fullName = ((results.firstName != null ? results.firstName : "") + " " +
                (results.lastName != null ? results.lastName : "")).trim();

        binding.txtName.setText(fullName);
        binding.txtPhone.setText(results.mobile != null ? "+91 " + results.mobile : "");

        if (results.profile != null && !results.profile.isEmpty()) {
            String imageUrl = "http://164.52.197.192:5678/uploads/" + results.profile;
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.log)
                    .into(binding.imgProfile);
        } else {
            binding.imgProfile.setImageResource(R.drawable.log);
        }

        Log.d(TAG, "Profile updated in UI: " + fullName + " | " + results.mobile);
    }

    private void showLogoutDialog() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logoutUser() {
        DocumentPrefs.clearAll(requireContext());

        new Thread(() -> {
            try {
                Glide.get(requireContext()).clearDiskCache();
            } catch (Exception ignored) {
            }
        }).start();
        Glide.get(requireContext()).clearMemory();

        File docsDir = new File(requireContext().getFilesDir(), "docs");
        if (docsDir.exists() && docsDir.isDirectory()) {
            for (File f : docsDir.listFiles()) {
                f.delete();
            }
        }

        Intent intent = new Intent(requireContext(), Sign_up.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        getProfileData();
    }
}
