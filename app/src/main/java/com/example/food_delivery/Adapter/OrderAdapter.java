package com.example.food_delivery.Adapter;

import android.content.Context;
import android.graphics.Color;
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
        void onConfirmPickup(OrderModel.ResultsBean order);

        void onItemToggle(OrderModel.ResultsBean order);
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
        OrderModel.ResultsBean order = orders.get(position);
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

            b.restaurantName.setText(order.getRestaurantData().getName());
            b.tvorderId.setText("Order No. #" + order.getOrderId());
            b.tvUserName.setText(order.getUserData() != null && order.getUserData().getFullName() != null
                    ? order.getUserData().getFullName()
                    : "Unknown");



            b.itemsContainer.removeAllViews();

            if (order.getDishes() != null && !order.getDishes().isEmpty()) {
                for (OrderModel.ResultsBean.DishesBean dish : order.getDishes()) {
                    View itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, null, false);

                    TextView text1 = itemView.findViewById(android.R.id.text1);
                    TextView text2 = itemView.findViewById(android.R.id.text2);


                    String dishName = dish.getName();
                    int qty = dish.getQuantity();
                    int price = dish.getPrice();

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



            b.tvTotalPrice.setText(String.valueOf("₹"+ order.getFinalPrice()));


            if (order.getRestaurantData().getAddress() != null && order.getRestaurantData().getAddress().isEmpty()) {
                b.tvAddress.setText("📍 " + order.getRestaurantData().getAddress());
            } else {
                b.tvAddress.setText("No address selected yet");
            }

//
//
            b.tvStatus.setText(order.getStatus());


            boolean expanded = order.isExpanded();
            b.tvUserName.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.layoutdetails.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.tvPickupLocation.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.paymentView.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.deliveryInfo.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.tvSelectOption.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.pickupView.setVisibility(expanded ? View.VISIBLE : View.GONE);


            b.ivexpandIcon.animate().rotation(expanded ? 180f : 0f).setDuration(200).start();


            View.OnClickListener toggleListener = v -> {
                boolean newExpandedState = !order.isExpanded();


                for (OrderModel.ResultsBean o : orders) {
                    o.setExpanded(false);
                }

                order.setExpanded(newExpandedState);
                notifyDataSetChanged();

                if (listener != null) listener.onItemToggle(order);
            };

            b.ivexpandIcon.setOnClickListener(toggleListener);
            b.getRoot().setOnClickListener(toggleListener);


            b.btnConfirmPickup.setOnClickListener(v -> {
                if (listener != null) listener.onConfirmPickup(order);
            });


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
