package com.example.food_delivery.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.view.animation.Interpolator;

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
    private GoogleMap liveGoogleMap;
    private LatLng lastLatLng; // 🆕 For movement check
    private Polyline routePolyline;


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
            // 🛑 Stop live updates and reset states
            stopLocationUpdates();
            isMapOpen = false;
            isUpdating = false;
            lastLatLng = null;

            if (liveGoogleMap != null) {
                liveGoogleMap.clear(); // clear markers and polylines
                liveGoogleMap = null;
            }

            // 🧭 Reset marker + polyline references
            partnerMarker = null;
            routePolyline = null;

            // 🧩 Switch UI back to order list
            binding.layoutMapTracking.setVisibility(View.GONE);
            binding.layoutOrderList.setVisibility(View.VISIBLE);
            binding.titleOrders.setVisibility(View.VISIBLE);
            binding.btnBack.setVisibility(View.GONE);

            Toast.makeText(requireContext(), "🛑 Tracking stopped", Toast.LENGTH_SHORT).show();
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

                        // ✅ Now open map only after accepted confirmation
                        openMapIfCoordinatesExist(order);


                    } else {
                        // ✅ Order came from API → hit accept API
                        Log.e("api hit", "123");
                        Toast.makeText(requireContext(), "✅ Accepted via api", Toast.LENGTH_SHORT).show();
                        callAcceptORRejctApi("accepted", selectedOrder.get_id());
                    }
                    setOrderDetailsData(selectedOrder);

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

    private void setOrderDetailsData(OrderModel.ResultsBean order) {
        try {
            if (getView() == null) return;

            // ✅ Restaurant Details
            if (order.getRestaurantData() != null) {
                binding.tvRestaurantName.setText("Name: " + (order.getRestaurantData().getName() != null ? order.getRestaurantData().getName() : "-"));
                binding.tvRestaurantAddress.setText("Address: " + (order.getRestaurantData().getAddress() != null ? order.getRestaurantData().getAddress() : "-"));
            }

            // ✅ Order Details
            binding.tvOrderId.setText("Order ID: " + (order.getOrderId() != null ? order.getOrderId() : "-"));
            binding.tvOrderAmount.setText(
                    "Amount: " + (order.getFinalPrice() > 0 ? "₹" + order.getFinalPrice() : "-")
            );

            // ✅ Delivery Details
            if (order.getRestaurantData().getAddress() != null) {
                binding.tvDeliveryAddress.setText("Delivery Address: " + order.getRestaurantData().getAddress());
            } else {
                binding.tvDeliveryAddress.setText("Delivery Address: -");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("setOrderDetailsData", "Error setting order details: " + e.getMessage());
        }
    }


    private void openMapIfCoordinatesExist(OrderModel.ResultsBean order) {
        if (order.getRestaurantData() != null &&
                order.getRestaurantData().getAddresslatLng() != null &&
                order.getRestaurantData().getAddresslatLng().getCoordinates() != null &&
                order.getRestaurantData().getAddresslatLng().getCoordinates().size() >= 2) {

            List<Double> coordinates = order.getRestaurantData().getAddresslatLng().getCoordinates();
            double restaurantLng = coordinates.get(0);
            double restaurantLat = coordinates.get(1);

            Log.e("restaurant long", String.valueOf(restaurantLng));
            Log.e("restaurant lat", String.valueOf(restaurantLat));

            openMapForSelectedOrder(restaurantLat, restaurantLng);
        } else {
            Log.e("OrderDebug", "⚠️ Missing restaurant coordinates — skipping map open.");
            Toast.makeText(requireContext(), "Restaurant location not available", Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("MissingPermission")
    private void openMapForSelectedOrder(double restaurantLat, double restaurantLng) {
        isMapOpen = true;
        binding.layoutOrderList.setVisibility(View.GONE);
        binding.layoutMapTracking.setVisibility(View.VISIBLE);
        binding.btnBack.setVisibility(View.VISIBLE);

        mapFragment.getMapAsync(googleMap -> {
            liveGoogleMap = googleMap;
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    restaurantLatLng = new LatLng(restaurantLat, restaurantLng);

                    googleMap.clear();

                    // 🟦 Add Delivery Partner Marker
                    partnerMarker = googleMap.addMarker(new MarkerOptions()
                            .position(selectedLatLng)
                            .title("🚴 Delivery Partner")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    // 🟥 Add Restaurant Marker
                    googleMap.addMarker(new MarkerOptions()
                            .position(restaurantLatLng)
                            .title("📍 Restaurant Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    drawRouteFromRestaurantToPartner(selectedLatLng, restaurantLatLng);

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

    /**
     * 🚗 Draws driving route from restaurant to driver using Google Directions API
     */
    private void drawRouteFromRestaurantToPartner(LatLng start, LatLng end) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&mode=driving&key=AIzaSyCBub0tSv16vf0M4D8rq-wfXATMPQQw3tY";

        new Thread(() -> {
            try {
                URL directionsUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) directionsUrl.openConnection();
                conn.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                Log.d("ROUTE_RESPONSE", sb.toString()); // 🧾 Log to verify response

                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONArray routes = jsonObject.getJSONArray("routes");

                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String points = overviewPolyline.getString("points");

                    List<LatLng> decodedPath = decodePoly(points);

                    requireActivity().runOnUiThread(() -> {
                        if (routePolyline != null) routePolyline.remove();
                        routePolyline = liveGoogleMap.addPolyline(new PolylineOptions()
                                .addAll(decodedPath)
                                .color(Color.BLUE)
                                .width(10f)
                                .geodesic(true));

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(start);
                        builder.include(end);
                        liveGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng((lat / 1E5), (lng / 1E5)));
        }
        return poly;
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
        req.setFastestInterval(2000);
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // ✅ Keep a reference so we can stop updates later
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null && partnerMarker != null) {

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    Log.d(TAG, "📍 Live GPS: " + lat + ", " + lng);

                    LatLng newLatLng = new LatLng(lat, lng);
                    // 🚴 If not moved
                    if (lastLatLng != null && distanceBetween(lastLatLng, newLatLng) < 3) { // within 3 meters
                        Log.d("MAP_TRACKING", "🟡 No movement detected.");
                        return;
                    }
                    lastLatLng = newLatLng; // update last known position

                    // ✅ Send to socket
                    sendSelectedLocationToSocket(lat, lng);

                    // ✅ Animate the marker smoothly instead of jumping
                    animateMarkerSmoothly(partnerMarker, newLatLng);

                    // ✅ If route not drawn yet, draw it now using Google Directions API
                    if (routePolyline == null) {
                        drawRouteFromRestaurantToPartner(restaurantLatLng, newLatLng);
                    } else {
                        // update polyline dynamically as driver moves
                        updateDriverPositionOnRoute(newLatLng);
                    }

                    // 🧮 Log live distance (optional)
                    float distance = calculateDistance(lat, lng, restaurantLatLng.latitude, restaurantLatLng.longitude);
                    Log.d("MAP_TRACKING", "🚴 Distance to restaurant: " + distance + " meters");

                }

            }
        };

        fusedLocationClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper());
    }

    /**
     * 🗺️ Update driver’s position on existing route
     */
    private void updateDriverPositionOnRoute(LatLng newLatLng) {
        if (routePolyline != null) {
            List<LatLng> points = routePolyline.getPoints();
            if (!points.isEmpty()) {
                points.set(0, newLatLng);
                routePolyline.setPoints(points);
            }
        }
    }

    private float calculateDistance(double startLat, double startLng, double endLat, double endLng) {
        float[] result = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, result);
        return result[0];
    }

    private float distanceBetween(LatLng start, LatLng end) {
        float[] result = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, result);
        return result[0];
    }

    private void animateMarkerSmoothly(Marker marker, LatLng toPosition) {
        final LatLng start = marker.getPosition();
        final long duration = 2000; // 2 seconds per move
        final Interpolator interpolator = new LinearInterpolator();
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                double lat = (toPosition.latitude - start.latitude) * t + start.latitude;
                double lng = (toPosition.longitude - start.longitude) * t + start.longitude;
                marker.setPosition(new LatLng(lat, lng));

                // Camera follows delivery partner
                liveGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                if (t < 1.0) {
                    handler.postDelayed(this, 16); // 60 FPS smooth
                }
            }
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

                        // ✅ Only open map if order accepted successfully
                        if (model.getMessage().contains("accepted")) {
                            if (selectedOrder != null) {
                                openMapIfCoordinatesExist(selectedOrder);
                            }
                        }

                        // 🔄 Refresh active order list
                        loadActiveOrders();

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
