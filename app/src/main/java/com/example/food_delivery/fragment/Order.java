package com.example.food_delivery.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.example.food_delivery.Model.AcceptRejectOrderModel;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private LatLng selectedLatLng;  // Delivery partner selected location
    private LatLng restaurantLatLng; // Restaurant location
    private Marker partnerMarker;   // For updating marker dynamically


    private FusedLocationProviderClient fusedLocationClient;
    private boolean isUpdating = false;
    private boolean isMapOpen = false;

    private Socket socket;
    private String partnerId;

    private static final String TAG = "SocketDebug";

    private OrderAdapter adapter;
    private SupportMapFragment mapFragment;
    private LocationCallback locationCallback;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);

        partnerId = DocumentPrefs.getPartnerId(requireContext());
        Log.d(TAG, "PartnerID = " + partnerId);


        //  Initialize socket manager
        SocketManager socketManager = SocketManager.getInstance();
        socket = socketManager.getSocket();

        //  Attach connect listener (emit location only after connected)
        socket.on(Socket.EVENT_CONNECT, args -> {
            Log.d(TAG, "✅ Socket connected, now sending initial location...");
            sendSelectedLocationToSocket(25.4891177, 74.3300726);
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            Log.e(TAG, "❌ Socket connection error: " + (args.length > 0 ? args[0] : "Unknown"));
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            Log.w(TAG, "⚠️ Socket disconnected: " + (args.length > 0 ? args[0] : "No reason"));
        });

        //  Listen for new orders
        socket.on("new_order", onNewOrderReceived);


        //  Connect socket AFTER listeners are attached
        socketManager.connect();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        callData();

        loadActiveOrders();

        return binding.getRoot();
    }

    private void callData() {
        // ✅ Setup persistent map fragment
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragmentContainer);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().add(R.id.mapFragmentContainer, mapFragment).commit();
        }
        binding.btnStart.setOnClickListener(v -> {
            if (selectedLatLng != null && restaurantLatLng != null) {
                Toast.makeText(requireContext(), "🚴 Delivery started!", Toast.LENGTH_SHORT).show();

                // Start live tracking updates
                startLocationUpdates();

                // Send initial location to socket
                sendSelectedLocationToSocket(selectedLatLng.latitude, selectedLatLng.longitude);

                // Optionally hide button or update UI
                binding.btnStart.setVisibility(View.GONE);
            } else {
                Toast.makeText(requireContext(), "Location not ready yet!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            binding.layoutMapTracking.setVisibility(View.GONE);
            binding.layoutOrderList.setVisibility(View.VISIBLE);
            binding.titleOrders.setVisibility(View.VISIBLE);
            binding.btnBack.setVisibility(View.GONE);
        });
    }

    private void loadActiveOrders() {
        if (isMapOpen) {
            Log.d(TAG, "Map is open — skipping order reload");
            return;
        }

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
                } else {
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
                    newOrder.setFromSocket(true);

                    // ✅ Fix: Restaurant info (since restaurantId is a String, not an object)
                    OrderModel.ResultsBean.RestaurantDataBean restData = new OrderModel.ResultsBean.RestaurantDataBean();
                    restData.set_id(obj.optString("restaurantId", ""));
                    restData.setName(obj.optString("restaurantName", "Unknown Restaurant"));
                    newOrder.setRestaurantData(restData);

                    // Pickup location
                    JSONObject pickupLoc = obj.optJSONObject("pickupLocation");
                    if (pickupLoc != null) {
                        double lat = pickupLoc.optDouble("lat", 0);
                        double lng = pickupLoc.optDouble("lng", 0);

                        OrderModel.ResultsBean.RestaurantDataBean.AddresslatLngBean latLngBean = new OrderModel.ResultsBean.RestaurantDataBean.AddresslatLngBean();
                        List<Double> coordinates = new ArrayList<>();
                        coordinates.add(lng); // index 0 → longitude
                        coordinates.add(lat);
                        latLngBean.setCoordinates(coordinates);


                        restData.setAddresslatLng(latLngBean);
                        newOrder.setRestaurantData(restData);
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
//                    loadActiveOrders();

                    Toast.makeText(requireContext(), "🆕 " + obj.optString("message", "New order received!"), Toast.LENGTH_SHORT).show();
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
        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(getContext(), orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onConfirmPickup(OrderModel.ResultsBean order) {
                selectedOrder = order;
                adapter.notifyDataSetChanged();
                if (selectedOrder != null) {

                    if (selectedOrder.isFromSocket()) {
                        // ✅ Order came from socket → only emit event
                        emitOrderResponseSocket(selectedOrder.get_id(), "accepted");
                        Toast.makeText(requireContext(), "✅ Accepted via Socket", Toast.LENGTH_SHORT).show();
                        Log.e("socket hit", "123");
                    } else {
                        // ✅ Order came from API → hit accept API
                        Log.e("api hit", "123");
                        Toast.makeText(requireContext(), "✅ Accepted via api", Toast.LENGTH_SHORT).show();
                        callAcceptORRejctApi("accepted", selectedOrder.get_id());
                    }

                    if (order.getRestaurantData() != null && order.getRestaurantData().getAddresslatLng() != null && order.getRestaurantData().getAddresslatLng().getCoordinates() != null && order.getRestaurantData().getAddresslatLng().getCoordinates().size() >= 2) {

                        List<Double> corrdinates = order.getRestaurantData().getAddresslatLng().getCoordinates();
                        double restaurantLng = corrdinates.get(0);
                        double restaurantLat = corrdinates.get(1);
                        Log.e("restaurant long", String.valueOf(restaurantLng));
                        Log.e("restaurant lat", String.valueOf(restaurantLat));
                        openMapForSelectedOrder(restaurantLat, restaurantLng);

                    } else {
                        Log.e("OrderDebug", "⚠️ Missing restaurant coordinates — skipping map open.");
                        Toast.makeText(requireContext(), "Restaurant location not available", Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(requireContext(), "Select an order first", Toast.LENGTH_SHORT).show();
//                showOrderDetail(order);
            }

            @Override
            public void onCancelPickup(OrderModel.ResultsBean order) {
                selectedOrder = order;
                if (selectedOrder != null) {
                    if (selectedOrder.isFromSocket()) {
                        emitOrderResponseSocket(selectedOrder.get_id(), "rejected");
                        Toast.makeText(requireContext(), "❌ Rejected via Socket", Toast.LENGTH_SHORT).show();
                    } else {
                        callAcceptORRejctApi("rejected", selectedOrder.get_id());
                    }
                } else {
                    Toast.makeText(requireContext(), "Select an order first", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onItemToggle(OrderModel.ResultsBean order) {
            }
        });
        binding.recyclerOrders.setAdapter(adapter);

    }

    @SuppressLint("MissingPermission")
    private void openMapForSelectedOrder(double restaurantLat, double restaurantLng) {
        isMapOpen = true;
        binding.layoutOrderList.setVisibility(View.GONE);
        binding.layoutMapTracking.setVisibility(View.VISIBLE);
        binding.btnBack.setVisibility(View.VISIBLE);

        mapFragment.getMapAsync(googleMap -> {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    restaurantLatLng = new LatLng(restaurantLat, restaurantLng);

                    googleMap.clear();

                    // 🟦 Add Delivery Partner Marker
                    partnerMarker = googleMap.addMarker(new MarkerOptions().position(selectedLatLng).title("🚴 Delivery Partner").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    // 🟥 Add Restaurant Marker
                    googleMap.addMarker(new MarkerOptions().position(restaurantLatLng).title("📍 Restaurant Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    // 🟩 Draw Polygon (area between partner & restaurant)
                    PolygonOptions polygonOptions = new PolygonOptions().add(selectedLatLng).add(new LatLng(selectedLatLng.latitude, restaurantLatLng.longitude)).add(restaurantLatLng).add(new LatLng(restaurantLatLng.latitude, selectedLatLng.longitude)).strokeColor(Color.BLUE).fillColor(0x220000FF).strokeWidth(4f);
                    googleMap.addPolygon(polygonOptions);

                    // 🟦 Draw connecting line
                    googleMap.addPolyline(new PolylineOptions().add(selectedLatLng).add(restaurantLatLng).width(6f).color(Color.BLUE));

                    // 🧭 Zoom camera to include both points
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(selectedLatLng);
                    builder.include(restaurantLatLng);
                    LatLngBounds bounds = builder.build();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));

                    // Enable My Location
                    googleMap.setMyLocationEnabled(true);

                } else {
                    Toast.makeText(requireContext(), "Unable to get partner location", Toast.LENGTH_SHORT).show();
                }
            });
        });
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

    // ✅ Start background location updates and move marker dynamically
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Enable location permission", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUpdating) return;
        isUpdating = true;

        LocationRequest req = LocationRequest.create();
        req.setInterval(3000); // every 3 sec
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // ✅ Keep a reference so we can stop updates later
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                double lat = locationResult.getLastLocation().getLatitude();
                double lng = locationResult.getLastLocation().getLongitude();
                Log.d(TAG, "📍 Live GPS: " + lat + ", " + lng);

                // ✅ Send to socket
                sendSelectedLocationToSocket(lat, lng);

                // ✅ Move marker on map if visible
                if (partnerMarker != null) {
                    partnerMarker.setPosition(new LatLng(lat, lng));
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(req, locationCallback, null);
    }

    // ✅ Stop location updates when needed (e.g., on delivery complete or fragment closed)
    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isUpdating = false;
            Log.d(TAG, "🛑 Location updates stopped");
        }
    }

    private void callAcceptORRejctApi(String status, String orderId) {
        OtpApi api = ApiClient.getClient().create(OtpApi.class);
        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId);
        body.put("status", status);

        String token = "Bearer " + DocumentPrefs.getToken(requireContext());
        Log.e("verify", "📤 Sending → " + body);
        Log.e("token", "📤 Token → " + token);

        Call<AcceptRejectOrderModel> call = api.updateAcceptRejectOrder(token, body);
        call.enqueue(new Callback<AcceptRejectOrderModel>() {
            @Override
            public void onResponse(Call<AcceptRejectOrderModel> call, Response<AcceptRejectOrderModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AcceptRejectOrderModel model = response.body();
                    if (model.isSuccess()) {
                        Toast.makeText(requireContext(), "✅ " + model.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("status updated", model.getMessage());
                        // 🔄 Refresh active order list
                        loadActiveOrders();

//                        emitOrderResponseSocket(orderId, status);
                    } else {
                        Toast.makeText(requireContext(), "❌ " + model.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("failed12", model.getMessage());
                    }
                } else {
                    Toast.makeText(requireContext(), "⚠️ " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("failed123", response.message());
                }
            }

            @Override
            public void onFailure(Call<AcceptRejectOrderModel> call, Throwable t) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show());
                }
                Log.e("OTP_VERIFY_FAILURE", "Error: " + t.getMessage(), t);
            }
        });
    }

    private void emitOrderResponseSocket(String orderId, String status) {
        try {
            if (socket == null || !socket.connected()) {
                Log.e(TAG, "❌ Socket not connected, cannot emit order_response");
                return;
            }
            if (partnerId == null || partnerId.isEmpty()) {
                Log.e(TAG, "❌ Partner ID missing, cannot emit order_response");
                return;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("orderId", orderId);
            jsonObject.put("partnerId", partnerId);
            jsonObject.put("status", status);

            socket.emit("order_response", jsonObject);

            Log.d(TAG, "📡 Emitted order_response → " + jsonObject.toString());

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error emitting order_response: " + e.getMessage());
        }
    }


    @Override
    public void onDestroyView() {
        stopLocationUpdates();
        super.onDestroyView();
        binding = null;
    }
}
