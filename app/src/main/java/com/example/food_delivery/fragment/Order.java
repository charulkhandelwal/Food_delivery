package com.example.food_delivery.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.food_delivery.Adapter.OrderAdapter;
import com.example.food_delivery.Model.OrderModel;
import com.example.food_delivery.R;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.Socket.SocketManager;
import com.example.food_delivery.databinding.FragmentOrderBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Order extends Fragment {

    private FragmentOrderBinding binding;
    private List<OrderModel> orderList;
    private OrderModel selectedOrder;

    private Marker selectedMarker;
    private String selectedAddress = "";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isUpdating = false;

    private Socket socket;
    private String partnerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);


        SocketManager socketManager = SocketManager.getInstance();
        socket = socketManager.getSocket();
        socketManager.connect();

        partnerId = DocumentPrefs.getPartnerId(requireContext());
        Log.d("PartnerID", "pid = " + partnerId);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());


        socket.on("new_order", onNewOrderReceived);

        setupDummyOrders();
        setupViews();

        return binding.getRoot();
    }


    private final Emitter.Listener onNewOrderReceived = args -> {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            try {
                JSONObject orderData = (JSONObject) args[0];
                String orderId = orderData.optString("orderId");
                String restaurantId = orderData.optString("restaurantId");
                String restaurantName = orderData.optString("restaurantName");
                JSONObject pickup = orderData.optJSONObject("pickupLocation");
                double lat = pickup != null ? pickup.optDouble("lat") : 0;
                double lng = pickup != null ? pickup.optDouble("lng") : 0;
                String message = orderData.optString("message");

                Log.d("SocketNewOrder", "🆕 New Order Received:");
                Log.d("SocketNewOrder", "orderId: " + orderId);
                Log.d("SocketNewOrder", "restaurantId: " + restaurantId);
                Log.d("SocketNewOrder", "restaurantName: " + restaurantName);
                Log.d("SocketNewOrder", "pickupLocation: " + lat + ", " + lng);
                Log.d("SocketNewOrder", "message: " + message);

                Toast.makeText(requireContext(),
                        "New Order: " + restaurantName + "\nMsg: " + message,
                        Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Log.e("SocketNewOrder", "Error parsing new_order: " + e.getMessage());
            }
        });
    };

    private void setupDummyOrders() {
        orderList = new ArrayList<>();
        orderList.add(new OrderModel("11250", "Pickup Center-1", "Order Items: 2x, 1x", "₹2300", "Nikhita Stores, Andheri East", "Pickup Pending"));
        orderList.add(new OrderModel("11251", "Pickup Center-2", "Order Items: Atta Ladoo", "₹150", "", "Pickup Rescheduled"));
        orderList.add(new OrderModel("11252", "Delivery", "Besan Ladoo - Qty 2", "₹1200", "", "Delivery Pending"));
    }

    private void setupViews() {
        binding.layoutNoOrders.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
        binding.layoutOrderList.setVisibility(orderList.isEmpty() ? View.GONE : View.VISIBLE);
        binding.layoutOrderDetail.setVisibility(View.GONE);
        binding.layoutMapTracking.setVisibility(View.GONE);

        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        OrderAdapter adapter = new OrderAdapter(getContext(), orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onConfirmPickup(OrderModel order) {
                selectedOrder = order;
                showOrderDetail(order);
            }

            @Override
            public void onItemToggle(OrderModel order) {}
        });

        binding.recyclerOrders.setAdapter(adapter);

        binding.btnConfirmPickup.setOnClickListener(v -> {
            if (selectedOrder != null) {
                openMapForSelectedOrder();
            } else {
                Toast.makeText(requireContext(), "Select an order first", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnStart.setOnClickListener(v -> {
            if (selectedAddress != null && !selectedAddress.isEmpty() && selectedOrder != null) {
                selectedOrder.setAddress(selectedAddress);
                Toast.makeText(requireContext(), "Delivery Started to " + selectedAddress, Toast.LENGTH_SHORT).show();
                startLocationUpdates();

                binding.layoutMapTracking.setVisibility(View.GONE);
                binding.layoutOrderList.setVisibility(View.VISIBLE);
                selectedOrder.setExpanded(true);
                binding.recyclerOrders.getAdapter().notifyDataSetChanged();
            } else {
                Toast.makeText(requireContext(), "Please select address on map", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOrderDetail(OrderModel order) {
        binding.layoutNoOrders.setVisibility(View.GONE);
        binding.layoutOrderList.setVisibility(View.GONE);
        binding.layoutOrderDetail.setVisibility(View.VISIBLE);
        binding.layoutMapTracking.setVisibility(View.GONE);

        binding.detailOrderId.setText("Order No. #" + order.getOrderId());
        binding.tvCustomerName.setText(order.getCustomerName());
        binding.tvOrderItems.setText(order.getItems());
        binding.tvOrderPrice.setText(order.getPrice());
        binding.tvDetailStatus.setText(order.getStatus());
        binding.selectaddress.setText(order.getAddress().isEmpty() ? "No address selected yet" : "📍 " + order.getAddress());
    }

    private void openMapForSelectedOrder() {
        binding.layoutOrderDetail.setVisibility(View.GONE);
        binding.layoutOrderList.setVisibility(View.GONE);
        binding.layoutMapTracking.setVisibility(View.VISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                LatLng defaultLocation = new LatLng(19.0760, 72.8777);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f));

                if (selectedMarker != null) selectedMarker.remove();
                selectedMarker = googleMap.addMarker(new MarkerOptions().position(defaultLocation).title("Tap to select"));

                googleMap.setOnMapClickListener(latLng -> {
                    if (selectedMarker != null) selectedMarker.remove();
                    selectedMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Delivery Location"));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                    selectedAddress = getAddressFromLatLng(latLng);
                    binding.tvMapSelectedAddress.setText(selectedAddress == null ? "Address not found" : "📍 " + selectedAddress);
                });
            });
        }
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
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

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Please enable location permission", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUpdating) return;
        isUpdating = true;

        LocationRequest req = LocationRequest.create();
        req.setInterval(2000);
        req.setFastestInterval(1000);
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                Location loc = locationResult.getLastLocation();
                if (loc != null) {
                    sendLocationToSocket(loc.getLatitude(), loc.getLongitude());
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(req, locationCallback, null);
    }

    private void sendLocationToSocket(double lat, double lng) {
        try {
            if (socket == null || !socket.connected()) {
                Log.e("SocketEmit", "Socket not connected!");
                return;
            }

            String data = partnerId + "," + lat + "," + lng;
            socket.emit("locationUpdate", data);
            Log.d("SocketEmit", "✅ Sent → event: locationUpdate | data: " + data);

        } catch (Exception e) {
            Log.e("SocketEmit", "Error while sending location: " + e.getMessage());
        }
    }

    private void stopLocationUpdates() {
        if (isUpdating && fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isUpdating = false;
            Log.d("Location", "Stopped updates");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLocationUpdates();
        if (socket != null) socket.off("new_order", onNewOrderReceived);
        binding = null;
    }
}
