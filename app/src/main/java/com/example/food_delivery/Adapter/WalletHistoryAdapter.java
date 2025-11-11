package com.example.food_delivery.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_delivery.Model.WalletHistoryModelResponse;
import com.example.food_delivery.databinding.WalletHistoryItemBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WalletHistoryAdapter extends RecyclerView.Adapter<WalletHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<WalletHistoryModelResponse.WalletDoc> walletList;

    public WalletHistoryAdapter(Context context, List<WalletHistoryModelResponse.WalletDoc> walletList) {
        this.context = context;
        this.walletList = walletList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        WalletHistoryItemBinding binding = WalletHistoryItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(walletList.get(position));
    }

    @Override
    public int getItemCount() {
        return walletList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final WalletHistoryItemBinding b;

        public ViewHolder(@NonNull WalletHistoryItemBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        public void bind(WalletHistoryModelResponse.WalletDoc model) {
            WalletHistoryModelResponse.OrderData order = model.getOrderData();

            if (order != null) {
                // ✅ User Name
                if (order.getUserData() != null && order.getUserData().getFirstName() != null) {
                    b.tvUserName.setText(order.getUserData().getFirstName());
                } else {
                    b.tvUserName.setText("Unknown User");
                }

                // ✅ Address
                b.tvUserAddress.setText(order.getRestaurantData() != null ?
                        order.getRestaurantData().getAddress() : "No Address");

                // ✅ Restaurant Name
                b.tvUserRestaurent.setText(order.getRestaurantData() != null ?
                        order.getRestaurantData().getName() : "Unknown Restaurant");

                // ✅ Order ID
                b.tvOrderId.setText("Order ID: " + (order.getOrderId() != null ? order.getOrderId() : "#N/A"));


                b.tvUserCommission.setText("Driver Commission: ₹" + model.getDriverCommission());

                // ✅ Date
                b.tvOrderTime.setText("Delivered on: " + formatDate(order.getCreatedAt()));

                // ✅ Payment Mode
                b.tvPaymentMode.setText(order.getPaymentMethod() != null ?
                        order.getPaymentMethod().toUpperCase() : "N/A");

                // ✅ Amount
                b.tvAmount.setText("₹" + model.getTransactionAmount());

                // ✅ Transaction ID
                b.tvTransactionId.setText("Txn ID: " + (model.getTransactionId() != null ? model.getTransactionId() : "N/A"));

                // ✅ Dishes List
                b.itemsContainer.removeAllViews();
                if (order.getDishes() != null && !order.getDishes().isEmpty()) {
                    for (WalletHistoryModelResponse.Dish dish : order.getDishes()) {
                        View dishView = LayoutInflater.from(context)
                                .inflate(android.R.layout.simple_list_item_2, b.itemsContainer, false);

                        TextView text1 = dishView.findViewById(android.R.id.text1);
                        TextView text2 = dishView.findViewById(android.R.id.text2);

                        text1.setText(dish.getName() + " (" + dish.getQuantity() + "x)");
                        text2.setText("₹" + dish.getPrice());

                        text1.setTextColor(Color.BLACK);
                        text2.setTextColor(Color.parseColor("#2E7D32"));

                        b.itemsContainer.addView(dishView);
                    }
                } else {
                    TextView empty = new TextView(context);
                    empty.setText("No Items Found");
                    empty.setTextColor(Color.GRAY);
                    b.itemsContainer.addView(empty);
                }
            }
        }

        private String formatDate(String dateStr) {
            if (dateStr == null) return "N/A";
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = input.parse(dateStr);
                SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                return output.format(date);
            } catch (ParseException e) {
                return dateStr;
            }
        }
    }
}
