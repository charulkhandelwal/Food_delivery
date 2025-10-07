package com.example.food_delivery.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.food_delivery.Activity.Refer_win;
import com.example.food_delivery.databinding.FragmentAccountBinding;

public class Account extends Fragment {

    private FragmentAccountBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        binding.icback1.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Refer_win.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
