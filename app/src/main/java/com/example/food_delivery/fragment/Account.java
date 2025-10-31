package com.example.food_delivery.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.food_delivery.Activity.EditProfileActivity;
import com.example.food_delivery.Activity.OrderHistoryAcitivity;
import com.example.food_delivery.Activity.Refer_win;
import com.example.food_delivery.Activity.Sign_up;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.FragmentAccountBinding;

public class Account extends Fragment {

    private FragmentAccountBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Open Refer & Win
        binding.icback1.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Refer_win.class);
            startActivity(intent);
        });

        // Edit Profile
        binding.rowEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        binding.tvOrderHistory.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(), OrderHistoryAcitivity.class);
            startActivity(intent);
        });

        // Logout
        binding.rowLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logoutUser() {

        DocumentPrefs.clearAll(requireContext());

        Intent intent = new Intent(requireContext(), Sign_up.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        requireActivity().finish();
    }
}
