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
import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
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
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Order extends Fragment {

    private FragmentOrderBinding binding;
    private List<OrderModel.ResultsBean> orderList = new ArrayList<>();
    private OrderModel.ResultsBean selectedOrder;

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

        partnerId = DocumentPrefs.getPartnerId(requireContext());
        Log.d(TAG, "PartnerID = " + partnerId);


        // ✅ Socket initialization
        SocketManager socketManager = SocketManager.getInstance();
        socket = socketManager.getSocket();

        // Attach fragment-specific listener before connect
        socket.on("new_order", onNewOrderReceived);

        // Connect socket
        socketManager.connect();
        sendSelectedLocationToSocket(25.4891177, 74.3300726);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        loadActiveOrders();
        return binding.getRoot();
    }

    private void loadActiveOrders() {
        OtpApi api = ApiClient.getClient().create(OtpApi.class);
        String token = DocumentPrefs.getToken(requireContext());

        Call<OrderModel> call = api.getActivOrders("Bearer " + token);
        call.enqueue(new Callback<OrderModel>() {
            @Override
            public void onResponse(Call<OrderModel> call, Response<OrderModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    orderList.clear();
                    orderList.addAll(response.body().getResults());
                    if (orderList.isEmpty()) {
                        binding.layoutNoOrders.setVisibility(View.VISIBLE);
                        binding.layoutOrderList.setVisibility(View.GONE);
                    } else {
                        binding.layoutNoOrders.setVisibility(View.GONE);
                        binding.layoutOrderList.setVisibility(View.VISIBLE);
                    }
                    setupAdapter();
                }
                else {
                    Log.e("API_RESPONSE_CODE", "Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("API_ERROR_BODY", errorBody);

                            JSONObject jsonObject = new JSONObject(errorBody);
                            String message = jsonObject.optString("message", "");

                            if (message.equalsIgnoreCase("Admin have deactivated or deleted your account.")) {
                                // 👉 Show custom message and hide order list
                                binding.layoutOrderList.setVisibility(View.GONE);
                                binding.layoutNoOrders.setVisibility(View.VISIBLE);
                                binding.nodata.setText("Please wait for admin approval.");
                            } else {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            }

                        } else if (response.body() != null) {
                            Log.e("API_RESPONSE_BODY", new Gson().toJson(response.body()));
                        } else {
                            Log.e("API_RESPONSE_BODY", "Body is null");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderModel> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final Emitter.Listener onNewOrderReceived = args -> {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            try {
                if (args.length > 0 && args[0] != null) {
                    Log.d("SocketNewOrder", "✅ new_order data received!");
                    Log.d("SocketNewOrder", "Raw Data → " + args[0].toString());

                    JSONObject obj = new JSONObject(args[0].toString());

                    // 🆕 Create a new temporary order model
                    OrderModel.ResultsBean newOrder = new OrderModel.ResultsBean();
                    newOrder.setOrderId(obj.optString("orderId", ""));
                    newOrder.setStatus("Pending");
//                    newOrder.setNew(true); // mark as new (for highlight, etc.)

                    // Restaurant info
                    JSONObject restaurantObj = obj.optJSONObject("restaurantId");
                    if (restaurantObj != null) {
                        OrderModel.ResultsBean.RestaurantDataBean restData =
                                new OrderModel.ResultsBean.RestaurantDataBean();
                        restData.set_id(restaurantObj.optString("_id", ""));
                        restData.setName(restaurantObj.optString("name", ""));
                        newOrder.setRestaurantData(restData);
                    }

                    // Pickup location
                    JSONObject pickupLoc = obj.optJSONObject("pickupLocation");
                    if (pickupLoc != null) {
                        OrderModel.ResultsBean.AddressBean addr =
                                new OrderModel.ResultsBean.AddressBean();
                        addr.setCity("Pickup @ " + obj.optString("restaurantName", "Unknown"));
                        newOrder.setAddress(addr);
                    }

                    // Add message as dummy price or info if needed
                    newOrder.setPaymentStatus(obj.optString("message", ""));

                    // 🧠 Add it to top of the list (like new order)
                    orderList.add(0, newOrder);

                    // 🧾 Update adapter
                    if (adapter != null) {
                        adapter.notifyItemInserted(0);
                        binding.recyclerOrders.scrollToPosition(0);
                    } else {
                        setupAdapter();
                    }

                    // 🌀 Optionally refresh via API to sync all
                    loadActiveOrders();

                    Toast.makeText(requireContext(),
                            "🆕 " + obj.optString("message", "New order received!"),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.w("SocketNewOrder", "⚠️ new_order event triggered but no data received!");
                }
            } catch (Exception e) {
                Log.e("SocketNewOrder", "❌ Error parsing new_order: " + e.getMessage());
                e.printStackTrace();
            }
        });
    };



    private void setupAdapter() {
        binding.layoutNoOrders.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
        binding.layoutOrderList.setVisibility(orderList.isEmpty() ? View.GONE : View.VISIBLE);
        binding.layoutMapTracking.setVisibility(View.GONE);

        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(getContext(), orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onConfirmPickup(OrderModel.ResultsBean order) {
                selectedOrder = order;
//                order.setNew(false);
                adapter.notifyDataSetChanged();
                if (selectedOrder != null) openMapForSelectedOrder();
                else
                    Toast.makeText(requireContext(), "Select an order first", Toast.LENGTH_SHORT).show();
//                showOrderDetail(order);
            }

            @Override
            public void onItemToggle(OrderModel.ResultsBean order) {}
        });
        binding.recyclerOrders.setAdapter(adapter);

       /* // ✅ Confirm Pickup → Open Map
        binding.btnConfirmPickup.setOnClickListener(v -> {

        });*/

        // ✅ Start button → test location + socket emit
        binding.btnStart.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                if (selectedOrder.getAddress() == null) {
                    selectedOrder.setAddress(new OrderModel.ResultsBean.AddressBean());
                }
                selectedOrder.getAddress().setCity(selectedAddress);
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
    private void showOrderDetail(OrderModel.ResultsBean order) {
        binding.layoutNoOrders.setVisibility(View.GONE);
        binding.layoutOrderList.setVisibility(View.GONE);
        binding.layoutMapTracking.setVisibility(View.GONE);
        // Convert dishes list to readable string
        StringBuilder itemsBuilder = new StringBuilder();
        if (order.getDishes() != null) {
            for (OrderModel.ResultsBean.DishesBean dish : order.getDishes()) {
                if (itemsBuilder.length() > 0) itemsBuilder.append(", ");
                itemsBuilder.append(dish.getPrice());
            }
        }
    }

    // ✅ Open Google Map to select delivery location
    private void openMapForSelectedOrder() {
        binding.layoutOrderList.setVisibility(View.GONE);
        binding.layoutMapTracking.setVisibility(View.VISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragmentContainer);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapFragmentContainer, mapFragment)
                    .commitNow(); // Important: attach immediately
        }

        mapFragment.getMapAsync(googleMap -> {
            LatLng defaultLocation = new LatLng(19.0760, 72.8777);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f));

            if (selectedMarker != null) selectedMarker.remove();
            selectedMarker = googleMap.addMarker(
                    new MarkerOptions().position(defaultLocation).title("Tap to select")
            );

            googleMap.setOnMapClickListener(latLng -> {
                if (selectedMarker != null) selectedMarker.remove();
                selectedMarker = googleMap.addMarker(
                        new MarkerOptions().position(latLng).title("Delivery Location")
                );
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

                selectedLatLng = latLng;
                selectedAddress = getAddressFromLatLng(latLng);
                binding.tvMapSelectedAddress.setText(
                        selectedAddress == null ? "Address not found" : "📍 " + selectedAddress
                );
            });
        });
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
