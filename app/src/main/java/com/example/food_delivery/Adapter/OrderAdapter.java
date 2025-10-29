package com.example.food_delivery.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_delivery.Model.OrderModel;
import com.example.food_delivery.databinding.OrderItemBinding;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderActionListener {
        void onConfirmPickup(OrderModel.ResultsBean order);    // User pressed Confirm Pickup
        void onCancelPickup(OrderModel.ResultsBean order);    // User pressed Confirm Pickup

        void onItemToggle(OrderModel.ResultsBean order);       // Item header clicked to expand/collapse
    }

    private final Context context;
    private final List<OrderModel.ResultsBean> orders;
    private final OnOrderActionListener listener;

    public OrderAdapter(Context context, List<OrderModel.ResultsBean> orders, OnOrderActionListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OrderItemBinding binding = OrderItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel.ResultsBean order = orders.get(position); // ✅ get current order
        holder.bind(order);

       /* // ✅ Highlight new orders
        if (order.isNew()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF9C4")); // Light yellow
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }*/
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private final OrderItemBinding b;

        public OrderViewHolder(@NonNull OrderItemBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        public void bind(final OrderModel.ResultsBean order) {
            // ✅ Basic info
            String restaurantName = (order.getRestaurantData() != null &&
                    order.getRestaurantData().getName() != null)
                    ? order.getRestaurantData().getName()
                    : (order.getRestaurantData() != null ? order.getRestaurantData().getName() : "Unknown");

            b.restaurantName.setText(restaurantName);

            b.tvorderId.setText("Order No. #" + order.getOrderId());
            b.tvUserName.setText(order.getUserData() != null && order.getUserData().getFullName() != null
                    ? order.getUserData().getFullName()
                    : "Unknown");


            // ✅ Dynamically add all dishes to the layout
            b.itemsContainer.removeAllViews();

            if (order.getDishes() != null && !order.getDishes().isEmpty()) {
                for (OrderModel.ResultsBean.DishesBean dish : order.getDishes()) {
                    View itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, null, false);

                    TextView text1 = itemView.findViewById(android.R.id.text1);
                    TextView text2 = itemView.findViewById(android.R.id.text2);

                    // Example fields - adjust based on your API model
                    String dishName = dish.getName(); // e.g. “Besan Ladoo”
                    int qty = dish.getQuantity();              // e.g. 2
                    int price = dish.getPrice();          // e.g. 500

                    text1.setText(dishName + "  (" + qty + "x)");
                    text2.setText("₹" + price);
                    text1.setTextColor(Color.BLACK);
                    text2.setTextColor(Color.parseColor("#4CAF50"));

                    b.itemsContainer.addView(itemView);
                }
            } else {
                TextView empty = new TextView(context);
                empty.setText("No items available");
                empty.setTextColor(Color.GRAY);
                b.itemsContainer.addView(empty);
            }


            // ✅ Price (convert int to string)
            b.tvTotalPrice.setText(String.valueOf("₹"+ order.getFinalPrice()));

            if (order.getRestaurantData() != null &&
                    !TextUtils.isEmpty(order.getRestaurantData().getAddress())) {
                b.tvAddress.setText("📍 " + order.getRestaurantData().getAddress());
            }else {
                b.tvAddress.setText("No address available");
            }


            if (order.getUserData() != null && order.getUserData().getAddresses() != null && !order.getUserData().getAddresses().isEmpty()) {
                b.tvPickupLocation.setText(order.getUserData().getAddresses().get(0).getCompleteAddress());
            } else {
                b.tvPickupLocation.setText("No address available");
            }


//
            b.tvStatus.setText(order.getStatus());

            // ✅ Handle expanded/collapsed view
            boolean expanded = order.isExpanded();
            b.tvUserName.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.layoutdetails.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.tvPickupLocation.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.paymentView.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.deliveryInfo.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.tvSelectOption.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.pickupView.setVisibility(expanded ? View.VISIBLE : View.GONE);

            // Rotate arrow with animation for better UX
            b.ivexpandIcon.animate().rotation(expanded ? 180f : 0f).setDuration(200).start();

            // ✅ Toggle expand/collapse
            View.OnClickListener toggleListener = v -> {
                boolean newExpandedState = !order.isExpanded();

                // Optional: collapse other items (only one expanded at a time)
                for (OrderModel.ResultsBean o : orders) {
                    o.setExpanded(false);
                }

                order.setExpanded(newExpandedState);
                notifyDataSetChanged(); // refresh all items

                if (listener != null) listener.onItemToggle(order);
            };

            b.ivexpandIcon.setOnClickListener(toggleListener);
            b.getRoot().setOnClickListener(toggleListener);

            // ✅ Confirm Pickup button
            b.btnConfirmPickup.setOnClickListener(v -> {
                if (listener != null) listener.onConfirmPickup(order);
            });

            b.btnCancelPickup.setOnClickListener(v -> {
                listener.onCancelPickup(order);
            });
            // ✅ Show "Select an Option" dialog
            b.tvSelectOption.setOnClickListener(v -> {
                String[] options = {"Pickup", "Delivered"};
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Choose an option")
                        .setItems(options, (dialog, which) -> {
                            String selected = options[which];
                            b.tvSelectOption.setText(selected);
                        })
                        .show();
            });

        }
    }
}
