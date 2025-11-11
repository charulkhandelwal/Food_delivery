package com.example.food_delivery.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_delivery.Model.OrderHistoryResponse;
import com.example.food_delivery.Model.OrderModel;
import com.example.food_delivery.databinding.ItemOrderHjistoryBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private final List<OrderHistoryResponse.OrderData> orderList;

    public OrderHistoryAdapter(List<OrderHistoryResponse.OrderData> orderList){
        this.orderList= orderList;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder{
        ItemOrderHjistoryBinding binding;
        public OrderViewHolder(ItemOrderHjistoryBinding binding){
            super(binding.getRoot());
            this.binding= binding;
        }
    }

    @NonNull
    @Override
    public OrderHistoryAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderHjistoryBinding binding= ItemOrderHjistoryBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryAdapter.OrderViewHolder holder, int position) {
        OrderHistoryResponse.OrderData order= orderList.get(position);
        ItemOrderHjistoryBinding b= holder.binding;

        b.tvRestaurantName.setText(order.getRestaurantId().getName());
        b.tvRestaurantAddress.setText(order.getRestaurantId().getAddress());
        b.tvOrderId.setText("Order ID: " + order.getOrderId());
        b.tvCustomerName.setText("Customer: " + order.getUserId().getFirstName() + " (" + order.getUserId().getMobile() + ")");
        b.tvAddress.setText("Delivered to: " +
                (order.getAddressId() != null && order.getAddressId().getCompleteAddress() != null
                        ? order.getAddressId().getCompleteAddress()
                        : "No address available") +
                (order.getAddressId() != null && order.getAddressId().getArea() != null
                        ? ", " + order.getAddressId().getArea()
                        : "")
        );        b.tvPrice.setText("₹" + order.getFinalPrice());


        String status= order.getStatus();
        b.tvStatus.setText(capitalize(status));

        switch (status.toLowerCase()) {
            case "delivered":
                b.tvStatus.setTextColor(0xFF4CAF50);
                break;
            case "cancelled":
                b.tvStatus.setTextColor(0xFFF44336);
                break;
            default:
                b.tvStatus.setTextColor(0xFFFF9800);
                break;
        }

        String formattedDate = "";
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date localDate = isoFormat.parse(order.getCreatedAt());

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            formattedDate = outputFormat.format(localDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        b.tvDate.setText(formattedDate);


        Glide.with(b.imgRestaurantLog.getContext())
                .load(order.getRestaurantId().getRestaurantLogo())
                .into(b.imgRestaurantLog);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}
