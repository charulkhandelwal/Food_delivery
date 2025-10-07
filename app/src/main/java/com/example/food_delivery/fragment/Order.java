package com.example.food_delivery.fragment;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.food_delivery.Adapter.OrderAdapter;
import com.example.food_delivery.Model.OrderModel;
import com.example.food_delivery.databinding.FragmentOrderBinding;
import com.example.food_delivery.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Order extends Fragment {

    private FragmentOrderBinding binding;
    private Marker selectedMarker;
    private String selectedAddress = "";
    private OrderModel selectedOrder;
    private List<OrderModel> orderList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);


        orderList = new ArrayList<>();
        orderList.add(new OrderModel("101", "Pizza Center", "2 x Pizza, 1 x Coke", "₹250", ""));
        orderList.add(new OrderModel("102", "Burger King", "1 x Burger, 1 x Fries", "₹180", ""));

        if (orderList.isEmpty()) {
            showNoOrders();
        } else {
            showOrderList();
        }

        return binding.getRoot();
    }

    private void showNoOrders() {
        binding.layoutNoOrders.setVisibility(View.VISIBLE);
        binding.layoutOrderList.setVisibility(View.GONE);
        binding.layoutOrderDetail.setVisibility(View.GONE);
        binding.layoutMapTracking.setVisibility(View.GONE);
    }

    private void showOrderList() {
        binding.layoutNoOrders.setVisibility(View.GONE);
        binding.layoutOrderList.setVisibility(View.VISIBLE);
        binding.layoutOrderDetail.setVisibility(View.GONE);
        binding.layoutMapTracking.setVisibility(View.GONE);

        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        OrderAdapter adapter = new OrderAdapter(orderList, order -> {
            // Open Map for selected order
            selectedOrder = order;
            showMapTracking();
        });

        binding.recyclerOrders.setAdapter(adapter);
    }

    private void showOrderDetail(OrderModel order) {
        selectedOrder = order;

        binding.layoutNoOrders.setVisibility(View.GONE);
        binding.layoutOrderList.setVisibility(View.GONE);
        binding.layoutOrderDetail.setVisibility(View.VISIBLE);
        binding.layoutMapTracking.setVisibility(View.GONE);

        binding.tvCustomerName.setText(order.getCustomerName());
        binding.tvOrderItems.setText(order.getItems());
        binding.tvOrderPrice.setText(order.getPrice());

        binding.selectaddress.setText(!order.getAddress().isEmpty() ? "📍 " + order.getAddress() : "No address selected yet");

        // Confirm Pickup opens map
        binding.btnConfirmPickup.setOnClickListener(v -> {
            selectedOrder = order;
            showMapTracking();
        });
    }

    private void showMapTracking() {
        binding.layoutNoOrders.setVisibility(View.GONE);
        binding.layoutOrderList.setVisibility(View.GONE);
        binding.layoutOrderDetail.setVisibility(View.GONE);
        binding.layoutMapTracking.setVisibility(View.VISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                LatLng defaultLocation = new LatLng(19.0760, 72.8777); // Mumbai
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f));

                if (selectedMarker != null) selectedMarker.remove();
                selectedMarker = googleMap.addMarker(new MarkerOptions()
                        .position(defaultLocation)
                        .title("Selected Location"));

                googleMap.setOnMapClickListener(latLng -> {
                    if (selectedMarker != null) selectedMarker.remove();
                    selectedMarker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Selected Location"));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

                    selectedAddress = getAddressFromLatLng(latLng);
                    binding.selectaddress.setText(selectedAddress != null ? "📍 " + selectedAddress : "Address not found");
                });
            });
        }

        binding.btnStart.setOnClickListener(v -> {
            if (!selectedAddress.isEmpty() && selectedOrder != null) {
                selectedOrder.setAddress(selectedAddress);
                Toast.makeText(getContext(), "Delivery Started 🚴 to " + selectedAddress, Toast.LENGTH_SHORT).show();

                binding.layoutMapTracking.setVisibility(View.GONE);
                binding.layoutOrderList.setVisibility(View.VISIBLE);


                selectedOrder.setExpanded(true);
                binding.recyclerOrders.getAdapter().notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Please select a location first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
