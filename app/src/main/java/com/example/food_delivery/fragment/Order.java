package com.example.food_delivery.fragment;

//import static androidx.graphics.shapes.Utils.distance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.nfc.Tag;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.food_delivery.Activity.MainActivity;
import com.example.food_delivery.Adapter.OrderAdapter;
import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.AcceptRejectOrderModel;
import com.example.food_delivery.Model.OrderModel;
import com.example.food_delivery.Model.ReachedRestaurantModel;
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
import org.json.JSONException;
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
    private LatLng destinationLatLng; // Restaurant location
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
    private String currentMapMode = "restaurant"; // or "user"
    private boolean isOrderInProgress = false;


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
            sendSelectedLocationToSocket(26.8456073, 75.8047004);
//            startLocationUpdates();
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

    /*private final Emitter.Listener onNewOrderReceived = args -> {
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

                    // ✅ Correct parsing for nested location
                    JSONObject userLoc = obj.optJSONObject("userAddress");
                    if (userLoc != null) {
                        JSONObject location = userLoc.optJSONObject("location");
                        if (location != null) {
                            JSONArray coordinatesArray = location.optJSONArray("coordinates");
                            if (coordinatesArray != null && coordinatesArray.length() >= 2) {
                                double lng = coordinatesArray.optDouble(0, 0.0);
                                double lat = coordinatesArray.optDouble(1, 0.0);

                                OrderModel.ResultsBean.DeliveryAddress deliveryAddress = new OrderModel.ResultsBean.DeliveryAddress();
                                List<Double> coordinates = new ArrayList<>();
                                coordinates.add(lng);
                                coordinates.add(lat);
                                deliveryAddress.setType(location.optString("type", "Point"));
                                deliveryAddress.setCoordinates(coordinates);

                                newOrder.setDeliveryAddressData(deliveryAddress);
                            }
                        }
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

                    Toast.makeText(requireContext(), "🆕 " + obj.optString("message", "New order received!"), Toast.LENGTH_SHORT).show();
                } else {
                    Log.w("SocketNewOrder", "⚠️ new_order event triggered but no data received!");
                }
            } catch (Exception e) {
                Log.e("SocketNewOrder", "❌ Error parsing new_order: " + e.getMessage());
                e.printStackTrace();
            }
        });
    };*/

    private final Emitter.Listener onNewOrderReceived = args -> {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            try {
                if (args.length > 0 && args[0] != null) {
                    Log.d("SocketNewOrder", "✅ new_order data received!");
                    Log.d("SocketNewOrder", "Raw Data → " + args[0].toString());

                    JSONObject obj = new JSONObject(args[0].toString());
                    OrderModel.ResultsBean newOrder = new OrderModel.ResultsBean();

                // 🆕 Basic order info
                    newOrder.setOrderId(obj.optString("orderId", ""));
                    newOrder.setStatus("Pending");
                    newOrder.setFromSocket(true);
                    newOrder.setNew(true);

                    // ✅ Full order details (main source of truth)
                    JSONObject fullOrder = obj.optJSONObject("fullOrderDetails");
                    if (fullOrder != null) {
                        newOrder.set_id(fullOrder.optString("_id", ""));
                        newOrder.setStatus(fullOrder.optString("status", "Pending"));
                        newOrder.setPaymentMethod(fullOrder.optString("paymentMethod", ""));
                        newOrder.setPaymentStatus(fullOrder.optString("paymentStatus", ""));
                        newOrder.setCreatedAt(fullOrder.optString("createdAt", ""));
                        newOrder.setUpdatedAt(fullOrder.optString("updatedAt", ""));
                        newOrder.setTotalPrice(fullOrder.optInt("totalPrice", 0));
                        newOrder.setFinalPrice(fullOrder.optInt("finalPrice", 0));
                        newOrder.setDiscountAmount(fullOrder.optInt("discountAmount", 0));
                        newOrder.setdriverReachedRestaurant(fullOrder.optBoolean("driverReachedRestaurant", false));

                        // ✅ Restaurant Data
                        JSONObject restObj = fullOrder.optJSONObject("restaurantData");
                        if (restObj != null) {
                            OrderModel.ResultsBean.RestaurantDataBean restData = new OrderModel.ResultsBean.RestaurantDataBean();
                            restData.set_id(restObj.optString("_id", ""));
                            restData.setName(restObj.optString("name", ""));
                            restData.setAddress(restObj.optString("address", ""));
                            restData.setZone(restObj.optString("zone", ""));
                            restData.setOwnerFullName(restObj.optString("ownerFullName", ""));
                            restData.setPhone(restObj.optString("phone", ""));
                            restData.setMaxDeliveryTime(restObj.optInt("maxDeliveryTime", 0));
                            restData.setMinDeliveryTime(restObj.optInt("minDeliveryTime", 0));

                            JSONArray cuisineArr = restObj.optJSONArray("cuisine");
                            if (cuisineArr != null) {
                                List<String> cuisines = new ArrayList<>();
                                for (int i = 0; i < cuisineArr.length(); i++)
                                    cuisines.add(cuisineArr.optString(i, ""));
                                restData.setCuisine(cuisines);
                            }

                            JSONObject addrLatLng = restObj.optJSONObject("addresslatLng");
                            if (addrLatLng != null) {
                                OrderModel.ResultsBean.RestaurantDataBean.AddresslatLngBean latLngBean = new OrderModel.ResultsBean.RestaurantDataBean.AddresslatLngBean();
                                latLngBean.setType(addrLatLng.optString("type", ""));
                                JSONArray coordArr = addrLatLng.optJSONArray("coordinates");
                                if (coordArr != null) {
                                    List<Double> coords = new ArrayList<>();
                                    for (int i = 0; i < coordArr.length(); i++)
                                        coords.add(coordArr.optDouble(i, 0));
                                    latLngBean.setCoordinates(coords);
                                }
                                restData.setAddresslatLng(latLngBean);
                            }

                            newOrder.setRestaurantData(restData);
                        }

                        // ✅ User Data
                        JSONObject userObj = fullOrder.optJSONObject("userData");
                        if (userObj != null) {
                            OrderModel.ResultsBean.UserData userData = new OrderModel.ResultsBean.UserData();
                            userData.set_id(userObj.optString("_id", ""));
                            userData.setFullName(userObj.optString("fullName", ""));
                            userData.setEmail(userObj.optString("email", ""));
                            userData.setMobile(userObj.optString("mobile", ""));

                            JSONArray addrArray = userObj.optJSONArray("address");
                            if (addrArray != null && addrArray.length() > 0) {
                                List<OrderModel.ResultsBean.Address> addressList = new ArrayList<>();
                                for (int i = 0; i < addrArray.length(); i++) {
                                    JSONObject addrObj = addrArray.optJSONObject(i);
                                    if (addrObj == null) continue;
                                    OrderModel.ResultsBean.Address addr = newOrder.new Address();
                                    addr.setCompleteAddress(addrObj.optString("completeAddress", ""));
                                    addr.setCountry(addrObj.optString("country", ""));
                                    addr.setZipCode(addrObj.optString("zipCode", ""));

                                    JSONObject locObj = addrObj.optJSONObject("location");
                                    if (locObj != null) {
                                        OrderModel.ResultsBean.Location location = newOrder.new Location();
                                        location.setType(locObj.optString("type", ""));
                                        JSONArray coordArray = locObj.optJSONArray("coordinates");
                                        if (coordArray != null && coordArray.length() == 2) {
                                            List<Double> coords = new ArrayList<>();
                                            coords.add(coordArray.optDouble(0));
                                            coords.add(coordArray.optDouble(1));
                                            location.setCoordinates(coords);
                                        }
                                        addr.setLocation(location);
                                    }
                                    addressList.add(addr);
                                }

                                userData.setAddresses(addressList);
                                newOrder.setUserData(userData);

                                if (!addressList.isEmpty()) {
                                    OrderModel.ResultsBean.DeliveryAddress deliveryAddr = new OrderModel.ResultsBean.DeliveryAddress();
                                    OrderModel.ResultsBean.Address firstAddr = addressList.get(0);
                                    if (firstAddr.getLocation() != null) {
                                        deliveryAddr.setType(firstAddr.getLocation().getType());
                                        deliveryAddr.setCoordinates(firstAddr.getLocation().getCoordinates());
                                    }
                                    newOrder.setDeliveryAddressData(deliveryAddr);
                                }
                            }
                        }

                        // ✅ Dishes
                        JSONArray dishesArray = fullOrder.optJSONArray("dishes");
                        if (dishesArray != null) {
                            List<OrderModel.ResultsBean.DishesBean> dishes = new ArrayList<>();
                            for (int i = 0; i < dishesArray.length(); i++) {
                                JSONObject dObj = dishesArray.optJSONObject(i);
                                if (dObj == null) continue;
                                OrderModel.ResultsBean.DishesBean dish = new OrderModel.ResultsBean.DishesBean();
                                dish.setName(dObj.optString("name", ""));
                                dish.setQuantity(dObj.optInt("quantity", 0));
                                dish.setPrice(dObj.optInt("price", 0));
                                dish.setSpecialInstructions(dObj.optString("specialInstructions", ""));
                                dishes.add(dish);
                            }
                            newOrder.setDishes(dishes);
                        }

                        // ✅ Stripe Payment
                        JSONObject stripe = fullOrder.optJSONObject("stripePayment");
                        if (stripe != null) {
                            OrderModel.ResultsBean.StripePaymentBean stripeBean = new OrderModel.ResultsBean.StripePaymentBean();
                            stripeBean.setId(stripe.optString("id", ""));
                            stripeBean.setAmount(stripe.optInt("amount", 0));
                            stripeBean.setCurrency(stripe.optString("currency", ""));
                            stripeBean.setStatus(stripe.optString("paymentStatus", ""));
                            stripeBean.setPayment_method(stripe.optString("payment_method", ""));
                            stripeBean.setCreated(stripe.optLong("created", 0));
                            newOrder.setStripePayment(stripeBean);
                        }
                    }

                    // ✅ If restaurantData is still null, fallback to top-level restaurantId
                    if (newOrder.getRestaurantData() == null) {
                        JSONObject restObj = obj.optJSONObject("restaurantId");
                        if (restObj != null) {
                            OrderModel.ResultsBean.RestaurantDataBean restData = new OrderModel.ResultsBean.RestaurantDataBean();
                            restData.set_id(restObj.optString("_id", ""));
                            restData.setName(restObj.optString("name", ""));
                            restData.setMaxDeliveryTime(restObj.optInt("maxDeliveryTime", 0));
                            restData.setMinDeliveryTime(restObj.optInt("minDeliveryTime", 0));
                            newOrder.setRestaurantData(restData);
                        }
                    }

                    // ✅ Add to list
                    orderList.add(0, newOrder);
                    if (adapter != null) {
                        adapter.notifyItemInserted(0);
                        binding.recyclerOrders.scrollToPosition(0);
                    } else {
                        setupAdapter();
                    }

                    Toast.makeText(requireContext(), "🆕 New Order: " + (newOrder.getRestaurantData() != null ? newOrder.getRestaurantData().getName() : "Unknown"), Toast.LENGTH_SHORT).show();


                } else {
                    Log.w("SocketNewOrder", "⚠️ new_order triggered but no data!");
                }

            } catch (Exception e) {
                Log.e("SocketNewOrder", "❌ Error parsing new_order: " + e.getMessage());
                e.printStackTrace();
            }
        });
    };


    /*private final Emitter.Listener onNewOrderReceived = args -> {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            try {
                if (args.length > 0 && args[0] != null) {
                    Log.d("SocketNewOrder", "✅ new_order data received!");
                    Log.d("SocketNewOrder", "Raw Data → " + args[0].toString());

                    // 🧠 Convert JSON → Model directly
                    Gson gson = new Gson();
                    OrderModel.ResultsBean newOrder = gson.fromJson(args[0].toString(), OrderModel.ResultsBean.class);

                    // Flag to indicate it's a new socket order
                    newOrder.setFromSocket(true);
                    newOrder.setNew(true);

                    // 🧾 Add at top
                    orderList.add(0, newOrder);

                    // 🔄 Refresh adapter
                    if (adapter != null) {
                        adapter.notifyItemInserted(0);
                        binding.recyclerOrders.scrollToPosition(0);
                    } else {
                        setupAdapter();
                    }

                    Toast.makeText(requireContext(),
                            "🆕 New Order Received: " + (newOrder.getRestaurantData() != null
                                    ? newOrder.getRestaurantData().getName()
                                    : "Unknown"),
                            Toast.LENGTH_SHORT).show();

                } else {
                    Log.w("SocketNewOrder", "⚠️ new_order triggered but no data!");
                }
            } catch (Exception e) {
                Log.e("SocketNewOrder", "❌ Error parsing new_order: " + e.getMessage());
                e.printStackTrace();
            }
        });
    };*/

    private void callData() {

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadActiveOrders();
            }
        });
        binding.btnPicked.setEnabled(false);

        // ✅ Setup persistent map fragment
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragmentContainer);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().add(R.id.mapFragmentContainer, mapFragment).commit();
        }
        binding.btnStart.setOnClickListener(v -> {
            if (selectedLatLng != null && destinationLatLng != null) {
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
            if (isOrderInProgress) {
                Toast.makeText(requireContext(),
                        "🚫 You cannot go back until the current order is completed.",
                        Toast.LENGTH_SHORT).show();
                return; // ❌ Do not go back
            }
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

        binding.btnReached.setOnClickListener(v -> {
            setupReachedButton();
        });

        binding.btnPicked.setOnClickListener(v -> {
            setupPickedOrder();

        });
        binding.btnOrder.setOnClickListener(v -> {
            setupOrderDelivered();
        });

        binding.btnPaymentReceive.setOnClickListener(v -> {
            setUpPayementReceive();
        });

        binding.btnSendAmountToAdmin.setOnClickListener(v->{
            setUpAdminPayment();
        });

        binding.btnOrderCompleted.setOnClickListener(v -> {
            setUpCompleteOrder();
        });

    }




    private void setupAdapter() {
        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(getContext(), orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onConfirmPickup(OrderModel.ResultsBean order) {
                selectedOrder = order;
                adapter.notifyDataSetChanged();
                ((MainActivity) requireActivity()).setOrderInProgress(true);
                if (selectedOrder != null) {
                    isOrderInProgress = true; // 🔒 Lock back navigation
                    resetOrderUIState();

                    if (selectedOrder.isFromSocket()) {
                        // ✅ Order came from socket → only emit event
                        emitOrderResponseSocket(order, "accepted");
                        Toast.makeText(requireContext(), "✅ Accepted via Socket", Toast.LENGTH_SHORT).show();
                        Log.e("socket hit", "123");
                        Log.e("orderId", selectedOrder.getOrderId());


                    } else {
                        // ✅ Order came from API → hit accept API
                        Log.e("api hit", "123");
                        Toast.makeText(requireContext(), "✅ Accepted via api", Toast.LENGTH_SHORT).show();
                        callAcceptORRejctApi("accepted", selectedOrder.getOrderId());
                    }
                    setOrderDetailsData(selectedOrder);

                } else
                    Toast.makeText(requireContext(), "Select an order first", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelPickup(OrderModel.ResultsBean order) {
                selectedOrder = order;
                ((MainActivity) requireActivity()).setOrderInProgress(false);
                if (selectedOrder != null) {
                    if (selectedOrder.isFromSocket()) {
                        emitOrderResponseSocket(order, "rejected");
                        Toast.makeText(requireContext(), "❌ Rejected via Socket", Toast.LENGTH_SHORT).show();
                    } else {
                        callAcceptORRejctApi("rejected", selectedOrder.getOrderId());
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
            binding.tvOrderAmount.setText("Amount: " + (order.getFinalPrice() > 0 ? "₹" + order.getFinalPrice() : "-"));

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

    private void loadActiveOrders() {
        if (isMapOpen) {
            binding.shimmerLayout.setVisibility(View.VISIBLE);
            binding.layoutMain.setVisibility(View.GONE);
            binding.shimmerLayout.startShimmer();
            Log.d(TAG, "Map is open — skipping order reload");
            return;
        }

        OtpApi api = ApiClient.getClient().create(OtpApi.class);
        String token = DocumentPrefs.getToken(requireContext());

        Call<OrderModel> call = api.getActivOrders("Bearer " + token);
        Log.d("ActiveOrderAPI", "📤 API Call Created: " + call.request().url());
        call.enqueue(new Callback<OrderModel>() {
            @Override
            public void onResponse(Call<OrderModel> call, Response<OrderModel> response) {

                binding.shimmerLayout.stopShimmer();
                binding.shimmerLayout.setVisibility(View.GONE);
                binding.layoutMain.setVisibility(View.VISIBLE);
               // binding.swipeRefreshLayout.setRefreshing(false);
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
                    binding.swipeRefreshLayout.setRefreshing(false);
                    setupAdapter();
                    Log.e("sucess active order api", response.message());

                } else {
                    Log.e("API_RESPONSE_CODE", "Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("API_ERROR_BODY", errorBody);

                            JSONObject jsonObject = new JSONObject(errorBody);
                            String message = jsonObject.optString("message", "");

                            if (message.equalsIgnoreCase("Admin have deactivated or deleted your account.")) {

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
                binding.shimmerLayout.stopShimmer();
                binding.shimmerLayout.setVisibility(View.GONE);
                binding.layoutMain.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpCompleteOrder() {
        if (selectedOrder.isFromSocket()) {
            emitCompletedOrderSocket();
            Toast.makeText(requireContext(), "✅ Accepted via Socket", Toast.LENGTH_SHORT).show();
            Log.e("Order complete socket hit", "123");
        } else {

            Log.e("Order complete api hit", "123");
            Toast.makeText(requireContext(), "✅ Accepted via api", Toast.LENGTH_SHORT).show();
            callOrderCompelteApi();
        }

        stopLocationUpdates();
        isMapOpen = false;
        isUpdating = false;
        loadActiveOrders();
        binding.recyclerOrders.setVisibility(View.VISIBLE);
        binding.layoutMapTracking.setVisibility(View.GONE);
    }

    private void setUpAdminPayment() {
        if (selectedOrder.isFromSocket()){
            Toast.makeText(requireContext(), "✅ Accepted via Socket", Toast.LENGTH_SHORT).show();
            Log.e("send amount to  admin socket hit", "123");
            emitSendAmountToAdmin();
        }else {

            Log.e("send amount to admin api hit", "123");
            Toast.makeText(requireContext(), "✅ Accepted via api", Toast.LENGTH_SHORT).show();
            callSendAmountToAdminHitApi();
        }
    }




    private void setUpPayementReceive() {
        if (selectedOrder.isFromSocket()) {
            emitPaymentReceive(selectedOrder.getOrderId(), partnerId, selectedOrder.getFinalPrice());
            Toast.makeText(requireContext(), "✅ Accepted via Socket", Toast.LENGTH_SHORT).show();
            Log.e("payment receipt socket hit", "123");
        } else {

            Log.e("payment api hit", "123");
            Toast.makeText(requireContext(), "✅ Accepted via api", Toast.LENGTH_SHORT).show();
            callPaymentReceiveHitApi();
        }
    }


    private void setupOrderDelivered() {
        if (selectedOrder.isFromSocket()) {

            emitDeliveredOrderResponseSocket(selectedOrder.getOrderId());
            Toast.makeText(requireContext(), "✅ Accepted via Socket", Toast.LENGTH_SHORT).show();
            Log.e("delivered socket hit", "123");

        } else {

            Log.e("delivered api hit", "123");
            Toast.makeText(requireContext(), "✅ Accepted via api", Toast.LENGTH_SHORT).show();
            callDeliveredCompleteHitApi();
        }

    }


    private void setupPickedOrder() {
        if (selectedOrder.isFromSocket()) {

            emitPicketOrderResponseSocket(selectedOrder.getOrderId());
            Toast.makeText(requireContext(), "✅ Accepted via Socket", Toast.LENGTH_SHORT).show();
            Log.e("socket hit", "123");

        } else {

            Log.e("api hit", "123");
            Toast.makeText(requireContext(), "✅ Accepted via api", Toast.LENGTH_SHORT).show();
            callPickedOrderFromRestaurantApi();
        }

        currentMapMode = "user";

        openMapForRestaturantToUserCorrdinates(selectedOrder);
        binding.llView.setVisibility(View.GONE);
        binding.btnStart.setVisibility(View.GONE);
        binding.btnOrder.setVisibility(View.VISIBLE);

    }

    private void setupReachedButton() {

        binding.btnReached.setOnClickListener(v -> {
            if (binding.btnReached.isEnabled()) {
                if (selectedOrder.isFromSocket()) {

                    emitReachedResponseSocket(selectedOrder.getOrderId());
                    Toast.makeText(requireContext(), "✅ Accepted via Socket", Toast.LENGTH_SHORT).show();
                    Log.e("socket hit", "123");

                } else {

                    Log.e("api hit", "123");
                    Toast.makeText(requireContext(), "✅ Accepted via api", Toast.LENGTH_SHORT).show();
                    callReachedApi();
                }

                binding.btnPicked.setEnabled(true);

            } else {
                Toast.makeText(requireContext(), "You're not close enough to the restaurant yet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callOrderCompelteApi() {
        OtpApi api = ApiClient.getClient().create(OtpApi.class);
        String token = DocumentPrefs.getToken(requireContext());

        String orderId = selectedOrder.getOrderId();
        Log.d("order complete api", "📦 Sending Order ID: " + orderId);
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Order ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId);
        body.put("partnerId", partnerId);

        Call<ReachedRestaurantModel> call = api.completeOrder("Bearer " + token, body);
        call.enqueue(new Callback<ReachedRestaurantModel>() {
            @Override
            public void onResponse(Call<ReachedRestaurantModel> call, Response<ReachedRestaurantModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReachedRestaurantModel result = response.body();

                    if (result.isSuccess()) {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                        resetOrderUIState();
                        isOrderInProgress = false; // 🔓 Unlock back navigation
                        ((MainActivity) requireActivity()).setOrderInProgress(false);

                    } else {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReachedRestaurantModel> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void callSendAmountToAdminHitApi() {
        OtpApi api= ApiClient.getClient().create(OtpApi.class);
        String token= DocumentPrefs.getToken(requireContext());
        String orderId = selectedOrder.getOrderId();
        String amount = String.valueOf(selectedOrder.getFinalPrice());

        Log.d("paid_order_to_admin api", "📦 Sending Order ID: " + orderId);
        Log.d("paid_order_to_admin api", "📦 Amount received: " + amount);

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Order ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body= new HashMap<>();
        body.put("orderId",orderId);
        body.put("partnerId",partnerId);
        body.put("amount",amount);

        Call<ReachedRestaurantModel> call= api.paymentReceivedByAdmin("Bearer "+token,body);
        call.enqueue(new Callback<ReachedRestaurantModel>() {
            @Override
            public void onResponse(Call<ReachedRestaurantModel> call, Response<ReachedRestaurantModel> response) {
                if (response.isSuccessful() && response.body()!= null){
                    ReachedRestaurantModel result= response.body();

                    if (result.isSuccess()) {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.btnPaymentReceive.setVisibility(View.GONE);
                        binding.btnSendAmountToAdmin.setVisibility(View.GONE);
                        binding.btnOrderCompleted.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReachedRestaurantModel> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void callPaymentReceiveHitApi() {
        OtpApi api = ApiClient.getClient().create(OtpApi.class);
        String token = DocumentPrefs.getToken(requireContext());

        String orderId = selectedOrder.getOrderId();
        String amountReceived = String.valueOf(selectedOrder.getFinalPrice());

        Log.d("payment api", "📦 Sending Order ID: " + orderId);
        Log.d("payment api", "💰 Amount received: " + amountReceived);

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Order ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId);
        body.put("amountReceived", amountReceived);

        Call<ReachedRestaurantModel> call = api.receivePayment("Bearer " + token, body);
        call.enqueue(new Callback<ReachedRestaurantModel>() {
            @Override
            public void onResponse(Call<ReachedRestaurantModel> call, Response<ReachedRestaurantModel> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "❌ Something went wrong!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ReachedRestaurantModel result = response.body();
                String message = result.getMessage() != null ? result.getMessage() : "";

                Log.d("payment api", "🧾 API Response: " + message);

                if (result.isSuccess()) {

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                    binding.btnPaymentReceive.setVisibility(View.GONE);
                    binding.btnSendAmountToAdmin.setVisibility(View.GONE);
                    binding.btnOrderCompleted.setVisibility(View.VISIBLE);

                } else if (message.equalsIgnoreCase("Wallet amount reached.")) {

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                    binding.btnPaymentReceive.setVisibility(View.GONE);
                    binding.btnSendAmountToAdmin.setVisibility(View.VISIBLE);
                    binding.btnOrderCompleted.setVisibility(View.GONE);

                } else {

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                    binding.btnPaymentReceive.setVisibility(View.VISIBLE);
                    binding.btnSendAmountToAdmin.setVisibility(View.GONE);
                    binding.btnOrderCompleted.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ReachedRestaurantModel> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "⚠️ Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void callDeliveredCompleteHitApi() {
        OtpApi api = ApiClient.getClient().create(OtpApi.class);
        String token = DocumentPrefs.getToken(requireContext());

        String orderId = selectedOrder.getOrderId();
        Log.d("Delivered api", "📦 Sending Order ID: " + orderId);

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Order ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId);

        Call<ReachedRestaurantModel> call = api.deliveredOrder("Bearer " + token, body);
        call.enqueue(new Callback<ReachedRestaurantModel>() {
            @Override
            public void onResponse(Call<ReachedRestaurantModel> call, Response<ReachedRestaurantModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReachedRestaurantModel result = response.body();

                    if (result.isSuccess()) {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.btnOrder.setVisibility(View.GONE);
                        binding.btnPaymentReceive.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReachedRestaurantModel> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void callReachedApi() {
        OtpApi api = ApiClient.getClient().create(OtpApi.class);
        String token = DocumentPrefs.getToken(requireContext());

        String orderId = selectedOrder.getOrderId();
        Log.d("REACHED_API", "📦 Sending Order ID: " + orderId);

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Order ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId); // or "order_id" if your backend expects it

        Call<ReachedRestaurantModel> call = api.reachedRestaurant("Bearer " + token, body);
        call.enqueue(new Callback<ReachedRestaurantModel>() {
            @Override
            public void onResponse(Call<ReachedRestaurantModel> call, Response<ReachedRestaurantModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReachedRestaurantModel result = response.body();

                    if (result.isSuccess()) {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.btnStart.setVisibility(View.GONE);
                        binding.llView.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReachedRestaurantModel> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void callPickedOrderFromRestaurantApi() {
        OtpApi api = ApiClient.getClient().create(OtpApi.class);
        String token = DocumentPrefs.getToken(requireContext());

        String orderId = selectedOrder.getOrderId();
        Log.d("PickedOrderFromRestaurant", "📦 Sending Order ID: " + orderId);

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Order ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId); // or "order_id" if your backend expects it

        Call<ReachedRestaurantModel> call = api.pickedOrderFromRestaurant("Bearer " + token, body);
        call.enqueue(new Callback<ReachedRestaurantModel>() {
            @Override
            public void onResponse(Call<ReachedRestaurantModel> call, Response<ReachedRestaurantModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReachedRestaurantModel result = response.body();

                    if (result.isSuccess()) {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.btnStart.setVisibility(View.GONE);
                        binding.llView.setVisibility(View.GONE);
                        binding.btnOrder.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReachedRestaurantModel> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        Log.e("API_NAME", "🔗 Endpoint: " + call.request().url());
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


    private void openMapIfCoordinatesExist(OrderModel.ResultsBean order) {
        if (order.getRestaurantData() != null && order.getRestaurantData().getAddresslatLng() != null && order.getRestaurantData().getAddresslatLng().getCoordinates() != null && order.getRestaurantData().getAddresslatLng().getCoordinates().size() >= 2) {

            List<Double> coordinates = order.getRestaurantData().getAddresslatLng().getCoordinates();
            double restaurantLng = coordinates.get(0);
            double restaurantLat = coordinates.get(1);

            Log.e("restaurant long", String.valueOf(restaurantLng));
            Log.e("restaurant lat", String.valueOf(restaurantLat));

            currentMapMode = "restaurant";
            openMapForSelectedOrder(restaurantLat, restaurantLng);
        } else {
            Log.e("OrderDebug", "⚠️ Missing restaurant coordinates — skipping map open.");
            Toast.makeText(requireContext(), "Restaurant location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMapForRestaturantToUserCorrdinates(OrderModel.ResultsBean order) {
        if (liveGoogleMap == null) return;


        liveGoogleMap.clear();
        routePolyline = null;
        partnerMarker = null;

        double userLat = 0.0;
        double userLng = 0.0;

        if (order != null && order.getDeliveryAddressData() != null && order.getDeliveryAddressData().getCoordinates() != null && order.getDeliveryAddressData().getCoordinates().size() >= 2) {

            List<Double> userCoordinates = order.getDeliveryAddressData().getCoordinates();
            userLng = userCoordinates.get(0);
            userLat = userCoordinates.get(1);

            Log.e("UserCoords", "✅ Using provided coordinates → Lat: " + userLat + ", Lng: " + userLng);
        } else {
            Log.e("UserCoords", "⚠️ Missing user coordinates — using default (0.0, 0.0)");
        }


        currentMapMode = "user";
        openMapForSelectedOrder(userLat, userLng);
    }

    @SuppressLint("MissingPermission")
    private void openMapForSelectedOrder(double destinationLat, double destinationLng) {
        isMapOpen = true;
        binding.layoutOrderList.setVisibility(View.GONE);
        binding.layoutMapTracking.setVisibility(View.VISIBLE);
        binding.btnBack.setVisibility(View.VISIBLE);


        binding.btnStart.setEnabled(false);
        binding.btnStart.setAlpha(0.5f);

        mapFragment.getMapAsync(googleMap -> {
            liveGoogleMap = googleMap;
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.e("selectedLatLng", "Lat: " + selectedLatLng.latitude + ", Lng: " + selectedLatLng.longitude);
                    destinationLatLng = new LatLng(destinationLat, destinationLng);
                    Log.e("detination lat lng", "Lat: " + destinationLatLng.latitude + ", Lng: " + destinationLatLng.longitude);

                    googleMap.clear();


                    partnerMarker = googleMap.addMarker(new MarkerOptions().position(selectedLatLng).title("🚴 Delivery Partner").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));


                    googleMap.addMarker(new MarkerOptions().position(destinationLatLng).title(currentMapMode.equals("restaurant") ? "📍 Restaurant" : "🏠 Delivery Location").icon(BitmapDescriptorFactory.defaultMarker(currentMapMode.equals("restaurant") ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_GREEN)));

                    drawRouteFromRestaurantToPartner(selectedLatLng, destinationLatLng);


                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(selectedLatLng);
                    builder.include(destinationLatLng);
                    LatLngBounds bounds = builder.build();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));

                    googleMap.setMyLocationEnabled(true);

                    binding.btnStart.setEnabled(true);
                    binding.btnStart.setAlpha(1f);

                } else {
                    Toast.makeText(requireContext(), "Unable to get partner location", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }



    private void drawRouteFromRestaurantToPartner(LatLng start, LatLng end) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + start.latitude + "," + start.longitude + "&destination=" + end.latitude + "," + end.longitude + "&mode=driving&key=AIzaSyCBub0tSv16vf0M4D8rq-wfXATMPQQw3tY";

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
                        routePolyline = liveGoogleMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.BLUE).width(10f).geodesic(true));

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


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null && partnerMarker != null) {

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    Log.d(TAG, "📍 Live GPS: " + lat + ", " + lng);

//                    sendSelectedLocationToSocket(lat, lng);
                    LatLng newLatLng = new LatLng(lat, lng);

                    if (lastLatLng != null && distanceBetween(lastLatLng, newLatLng) < 3) { // within 3 meters
                        Log.d("MAP_TRACKING", "🟡 No movement detected.");
                        return;
                    }
                    lastLatLng = newLatLng; // update last known position

                    sendSelectedLocationToSocket(lat, lng);


                    animateMarkerSmoothly(partnerMarker, newLatLng);

                    if (routePolyline == null) {
                        drawRouteFromRestaurantToPartner(destinationLatLng, newLatLng);
                    } else {
                        // update polyline dynamically as driver moves
                        updateDriverPositionOnRoute(newLatLng);
                    }

                    // 🧮 Log live distance (optional)
                    float distance = calculateDistance(lat, lng, destinationLatLng.latitude, destinationLatLng.longitude);
                    Log.d("MAP_TRACKING", "🚴 Distance to restaurant: " + distance + " meters");

                    if (distance < 10) {

                        binding.btnReached.setEnabled(true);
                        binding.btnReached.setAlpha(1f);
                    } else {

                        binding.btnReached.setEnabled(false);
                        binding.btnReached.setAlpha(0.5f);
                    }

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



    private void emitOrderResponseSocket(OrderModel.ResultsBean order, String status) {
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
            jsonObject.put("orderId", order.getOrderId());
            jsonObject.put("partnerId", partnerId);
            jsonObject.put("status", status);

            socket.emit("order_response", jsonObject);
            Log.d(TAG, "📡 Emitted order_response → " + jsonObject);

            // ✅ Listen for backend confirmation only once
            socket.once("order_taken", args -> {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        String orderId = data.optString("orderId");
                        String responseStatus = data.optString("status");

                        Log.d("SocketOrderTaken", "📩 Received order_taken → " + data);

                        if (responseStatus.contains("You have already accepted this order.")) {
                            Toast.makeText(getContext(), "⚠️ You have already accepted this order.", Toast.LENGTH_SHORT).show();
                        } else if (responseStatus.contains("You have an active order. Cannot accept new orders.")) {
                            Toast.makeText(getContext(), "🚫 You already have an active order.", Toast.LENGTH_SHORT).show();
                        } else {
                            // ✅ No conflict — open map now
                            openMapIfCoordinatesExist(order);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error emitting order_response: " + e.getMessage());
        }
    }

    private void emitSendAmountToAdmin() {
        try {
            if (socket == null || !socket.connected()){
                Log.e(TAG, "Socket not connected, cannot emit paid_order_to_admin");
                return;
            }

            if (partnerId == null || partnerId.isEmpty()){
                Log.e(TAG, "❌ Partner ID missing, cannot emit paid_order_to_admin");
                return;
            }

            JSONObject jsonObject= new JSONObject();
            jsonObject.put("orderId", selectedOrder.getOrderId());
            jsonObject.put("partnerId", partnerId);
            jsonObject.put("amount", selectedOrder.getFinalPrice());
            socket.emit("paid_order_to_admin",jsonObject);
            Log.e(TAG,"📡 Emitted paid_order_to_admin → "+jsonObject.toString());

            binding.btnPaymentReceive.setVisibility(View.GONE);
            binding.btnSendAmountToAdmin.setVisibility(View.GONE);
            binding.btnOrderCompleted.setVisibility(View.VISIBLE);

        }catch (Exception e){
            Log.e(TAG,"Error emitting sendAmount "+e.getMessage());
        }
    }


    private void emitCompletedOrderSocket() {
        try {
            if (socket == null || !socket.connected()) {
                Log.e(TAG, "❌ Socket not connected, cannot emit payment_received");
                return;
            }
            if (partnerId == null || partnerId.isEmpty()) {
                Log.e(TAG, "❌ Partner ID missing, cannot emit payment_received");
                return;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("orderId", selectedOrder.getOrderId());
            jsonObject.put("partnerId", partnerId);

            socket.emit("order_completed", jsonObject);
            Log.d(TAG, "📡 Emitted order_completed → " + jsonObject.toString());
            isOrderInProgress = false; // 🔓 Unlock back navigation

            resetOrderUIState();
            ((MainActivity) requireActivity()).setOrderInProgress(false);

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error emitting order_completed: " + e.getMessage());
        }
    }

    private void emitPaymentReceive(String orderId, String partnerId, int finalPrice) {
        try {
            if (socket == null || !socket.connected()) {
                Log.e(TAG, "❌ Socket not connected, cannot emit payment_received");
                return;
            }
            if (partnerId == null || partnerId.isEmpty()) {
                Log.e(TAG, "❌ Partner ID missing, cannot emit payment_received");
                return;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("orderId", orderId);
            jsonObject.put("partnerId", partnerId);
            jsonObject.put("amount", finalPrice);

            // ✅ Emit event
            socket.emit("payment_received", jsonObject);
            Log.d(TAG, "📡 Emitted payment_received → " + jsonObject.toString());

            // Flag to detect if payment failed
            final boolean[] paymentFailed = {false};

            // ❌ insufficient_payment event
            socket.once("insufficient_payment", args -> {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    try {
                        if (args.length == 0 || args[0] == null) return;
                        JSONObject data = (JSONObject) args[0];
                        String responseStatus = data.optString("status");
                        Log.w(TAG, "⚠️ insufficient_payment → " + responseStatus);

                        paymentFailed[0] = true;
                        Toast.makeText(getContext(), responseStatus, Toast.LENGTH_LONG).show();

                        // Keep payment button visible, hide delivery button
                        binding.btnPaymentReceive.setVisibility(View.VISIBLE);
                        binding.btnOrderCompleted.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "❌ Error in insufficient_payment handler: " + e.getMessage());
                    }
                });
            });


            socket.once("wallet_limit_exceeded", args -> {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    try {
                        if (args.length == 0 || args[0] == null) return;
                        JSONObject data = (JSONObject) args[0];
                        String responseStatus = data.optString("status");
                        Log.w(TAG, "⚠️ wallet_limit_exceeded → " + responseStatus);

                        paymentFailed[0] = true;
                        Toast.makeText(getContext(), responseStatus, Toast.LENGTH_LONG).show();


                        binding.btnPaymentReceive.setVisibility(View.GONE);
                        binding.btnSendAmountToAdmin.setVisibility(View.VISIBLE);
                        binding.btnOrderCompleted.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "❌ Error in wallet_limit_exceeded handler: " + e.getMessage());
                    }
                });
            });


            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!paymentFailed[0]) {
                    Log.d(TAG, "✅ No errors received → Payment success assumed");
                    binding.btnPaymentReceive.setVisibility(View.GONE);
                    binding.btnOrderCompleted.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Payment received successfully!", Toast.LENGTH_SHORT).show();
                }
            }, 1500);

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error emitting payment_received: " + e.getMessage());
        }
    }


    private void emitReachedResponseSocket(String orderId) {
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

            socket.emit("reached_restaurant", jsonObject);

            Log.d(TAG, "📡 Emitted reached_restaurant → " + jsonObject.toString());

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error emitting reached_restaurant: " + e.getMessage());
        }
    }

    private void emitPicketOrderResponseSocket(String orderId) {
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

            socket.emit("picked_order", jsonObject);

            Log.d(TAG, "📡 Emitted picked_order → " + jsonObject.toString());

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error emitting picked_order: " + e.getMessage());
        }
    }

    private void emitDeliveredOrderResponseSocket(String orderId) {
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

            socket.emit("delivered_order", jsonObject);
            Log.d(TAG, "📡 Emitted delivered_order → " + jsonObject.toString());
            Toast.makeText(requireContext(), "✅ Delivered Order", Toast.LENGTH_SHORT).show();


            // 🔹 COD Payment Logic
            if (selectedOrder.getPaymentMethod().equalsIgnoreCase("cod")) {

                // Initially hide other buttons
                binding.btnOrder.setVisibility(View.GONE);
                binding.btnOrderCompleted.setVisibility(View.GONE);
                binding.btnPaymentReceive.setVisibility(View.GONE);

                socket.once("collectCODPayment", args -> {
                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(() -> {
                        try {
                            if (args.length == 0 || args[0] == null) {
                                Log.w(TAG, "⚠️ collectCODPayment received empty data");
                                return;
                            }

                            JSONObject data = (JSONObject) args[0];
                            String receivedOrderId = data.optString("orderId", "");
                            int amount = data.optInt("amount", 0);
                            String message = data.optString("status", "");

                            Log.d(TAG, "💰 collectCODPayment received → OrderID: " + receivedOrderId +
                                    ", Amount: " + amount + ", Message: " + message);

                            // 🔹 Show Toast to collect COD
                            if (amount > 0) {
                                String collectMsg = "💵 Collect ₹" + amount + " from the user";
                                Toast.makeText(requireContext(), collectMsg, Toast.LENGTH_LONG).show();

                                // 🔹 Update UI: show payment receive button only
                                binding.btnOrder.setVisibility(View.GONE);
                                binding.llView.setVisibility(View.GONE);
                                binding.btnOrderCompleted.setVisibility(View.GONE);
                                binding.btnPaymentReceive.setVisibility(View.VISIBLE);
                            } else {
                                Log.w(TAG, "⚠️ COD amount invalid (0)");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "❌ Error in collectCODPayment handler: " + e.getMessage());
                        }
                    });
                });

            } else {
                // 🔹 For prepaid orders → directly show delivery complete
                binding.btnOrder.setVisibility(View.GONE);
                binding.btnOrderCompleted.setVisibility(View.VISIBLE);
                binding.btnPaymentReceive.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error emitting delivered_order: " + e.getMessage());
        }
    }



    private void updatePaymentStatus(String orderId, int amount) {
        // Example: Update order list or notify adapter
        for (OrderModel.ResultsBean order : orderList) {
            if (order.getOrderId().equals(orderId)) {
                order.setPaymentStatus("received");
                order.setFinalPrice(amount);
                break;
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        stopLocationUpdates();
        super.onDestroyView();
        binding = null;
    }


    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "🛑 Location updates stopped.");
        }

        isUpdating = false;
        lastLatLng = null;
        locationCallback = null;


        if (liveGoogleMap != null) {
            if (partnerMarker != null) {
                partnerMarker.remove();
                partnerMarker = null;
            }
            if (routePolyline != null) {
                routePolyline.remove();
                routePolyline = null;
            }
        }
    }

    private void resetOrderUIState() {
        binding.btnStart.setVisibility(View.VISIBLE);          // show Start/Accept
        binding.btnOrder.setVisibility(View.GONE);          // show Start/Accept
        binding.btnOrderCompleted.setVisibility(View.GONE);    // hide Complete
        binding.btnPaymentReceive.setVisibility(View.GONE);    // hide Payment button
        binding.llView.setVisibility(View.VISIBLE);            // show details if hidden
        binding.btnSendAmountToAdmin.setVisibility(View.GONE);

    }
}
