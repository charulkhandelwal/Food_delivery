package com.example.food_delivery.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import com.google.android.gms.location.LocationRequest;
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

public class Order extends Fragment {

    private FragmentOrderBinding binding;
    private List<OrderModel> orderList;
    private OrderModel selectedOrder;

    private Marker selectedMarker;
    private LatLng selectedLatLng;
    private String selectedAddress = "";

    private FusedLocationProviderClient fusedLocationClient;
    private boolean isUpdating = false;

    private Socket socket;
    private String partnerId;

    private static final String TAG = "SocketDebug";

    private OrderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);

        // ✅ Setup socket connection
        SocketManager socketManager = SocketManager.getInstance();
        socket = socketManager.getSocket();
        socketManager.connect();

        partnerId = DocumentPrefs.getPartnerId(requireContext());
        Log.d(TAG, "Partner ID = " + partnerId);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // ✅ Socket status logs
        socket.on(Socket.EVENT_CONNECT, args -> Log.d(TAG, "✅ Socket connected"));
        socket.on(Socket.EVENT_DISCONNECT, args -> Log.d(TAG, "⚠️ Socket disconnected"));
        socket.on(Socket.EVENT_CONNECT_ERROR, args -> Log.e(TAG, "❌ Socket connect error: " + args[0]));

        setupDummyOrders();
        setupViews();

        // ✅ Listen for new orders from socket
        socket.on("new_order", args -> {
            if (args.length > 0) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String orderId = data.optString("orderId");
                    String restaurantName = data.optJSONObject("restaurant") != null
                            ? data.getJSONObject("restaurant").optString("name")
                            : "";
                    JSONObject pickupLocation = data.optJSONObject("pickupLocation");
                    double lat = pickupLocation != null ? pickupLocation.optDouble("lat") : 0;
                    double lng = pickupLocation != null ? pickupLocation.optDouble("lng") : 0;

                    String message = data.optString("message");

                    Log.d(TAG, " New Order Received");
                    Log.d(TAG, "Order ID: " + orderId);
                    Log.d(TAG, "Restaurant Name: " + restaurantName);
                    Log.d(TAG, "Pickup Lat/Lng: " + lat + ", " + lng);
                    Log.d(TAG, "Message: " + message);

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "📦 New Order: " + message, Toast.LENGTH_LONG).show();

                        OrderModel newOrder = new OrderModel(orderId, restaurantName, "2x Item", "₹250", lat + ", " + lng, "Pickup Pending");
                        newOrder.setNew(true);
                        orderList.add(0, newOrder);
                        adapter.notifyItemInserted(0);
                        binding.recyclerOrders.scrollToPosition(0);
                        binding.layoutNoOrders.setVisibility(View.GONE);
                    });

                } catch (Exception e) {
                    Log.e(TAG, "⚠️ Error parsing new_order: " + e.getMessage());
                }
            }
        });

        return binding.getRoot();
    }

    // ✅ Dummy data
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
        adapter = new OrderAdapter(getContext(), orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onConfirmPickup(OrderModel order) {
                selectedOrder = order;
                order.setNew(false);
                adapter.notifyDataSetChanged();
                showOrderDetail(order);
            }

            @Override
            public void onItemToggle(OrderModel order) {}
        });
        binding.recyclerOrders.setAdapter(adapter);

        // ✅ Confirm Pickup → Open Map
        binding.btnConfirmPickup.setOnClickListener(v -> {
            if (selectedOrder != null) openMapForSelectedOrder();
            else Toast.makeText(requireContext(), "Select an order first", Toast.LENGTH_SHORT).show();
        });

        // ✅ Start button → test location + socket emit
        binding.btnStart.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                selectedOrder.setAddress(selectedAddress);
                Toast.makeText(requireContext(), "Delivery Started to " + selectedAddress, Toast.LENGTH_SHORT).show();
                sendSelectedLocationToSocket(selectedLatLng.latitude, selectedLatLng.longitude);
                startLocationUpdates();
                binding.layoutMapTracking.setVisibility(View.GONE);
                binding.layoutOrderList.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(requireContext(), "Please tap on map to select location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Show order detail view
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

    // ✅ Open Google Map to select delivery location
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

                    selectedLatLng = latLng;
                    selectedAddress = getAddressFromLatLng(latLng);
                    binding.tvMapSelectedAddress.setText(selectedAddress == null ? "Address not found" : "📍 " + selectedAddress);
                });
            });
        }
    }

    // ✅ Convert lat/lng → readable address
    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error: " + e.getMessage());
        }
        return null;
    }

    // ✅ Emit live location to socket
    private void sendSelectedLocationToSocket(double lat, double lng) {
        try {
            if (partnerId == null || partnerId.isEmpty()) {
                Log.e(TAG, "❌ Partner ID missing, cannot emit location");
                return;
            }
            if (socket == null || !socket.connected()) {
                Log.e(TAG, "❌ Socket not connected, cannot emit location");
                return;
            }

            JSONObject locationData = new JSONObject();
            locationData.put("partnerId", partnerId);
            locationData.put("lat", lat);
            locationData.put("lng", lng);

            Log.d(TAG, "📡 Sending locationUpdate: " + locationData);
            socket.emit("locationUpdate", locationData);

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error emitting locationUpdate: " + e.getMessage());
        }
    }

    // ✅ Start background location updates
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Enable location permission", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUpdating) return;
        isUpdating = true;

        LocationRequest req = LocationRequest.create();
        req.setInterval(3000);
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        com.google.android.gms.location.LocationCallback locationCallback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                if (locationResult == null) return;

                double lat = locationResult.getLastLocation().getLatitude();
                double lng = locationResult.getLastLocation().getLongitude();
                Log.d(TAG, "📍 Live GPS: " + lat + ", " + lng);
                sendSelectedLocationToSocket(lat, lng);
            }
        };

        fusedLocationClient.requestLocationUpdates(req, locationCallback, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
