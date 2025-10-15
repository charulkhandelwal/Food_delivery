package com.example.food_delivery.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_delivery.Model.OrderModel;
import com.example.food_delivery.databinding.OrderItemBinding;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderActionListener {
        void onConfirmPickup(OrderModel order);    // User pressed Confirm Pickup
        void onItemToggle(OrderModel order);       // Item header clicked to expand/collapse
    }

    private final Context context;
    private final List<OrderModel> orders;
    private final OnOrderActionListener listener;

    public OrderAdapter(Context context, List<OrderModel> orders, OnOrderActionListener listener) {
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
        OrderModel order = orders.get(position); // ✅ get current order
        holder.bind(order);

        // ✅ Highlight new orders
        if (order.isNew()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF9C4")); // Light yellow
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
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

        public void bind(final OrderModel order) {
            b.tvorderId.setText("Order No. #" + order.getOrderId());
            b.tvcustomerName.setText(order.getCustomerName());
            b.tvItems.setText(order.getItems());
            b.tvprice.setText(order.getPrice());
            b.tvAddress.setText(order.getAddress().isEmpty() ? "No address selected yet" : "📍 " + order.getAddress());
            b.tvStatus.setText(order.getStatus());

            // Expand/collapse details
            boolean expanded = order.isExpanded();
            b.layoutdetails.setVisibility(expanded ? android.view.View.VISIBLE : android.view.View.GONE);
            b.ivexpandIcon.setRotation(expanded ? 180f : 0f);

            // Expand/collapse on icon click
            b.ivexpandIcon.setOnClickListener(v -> {
                order.setExpanded(!order.isExpanded());
                notifyItemChanged(getAdapterPosition());
                if (listener != null) listener.onItemToggle(order);
            });

            // Expand/collapse on item click
            b.getRoot().setOnClickListener(v -> {
                order.setExpanded(!order.isExpanded());
                notifyItemChanged(getAdapterPosition());
                if (listener != null) listener.onItemToggle(order);
            });

            // Confirm Pickup button
            b.btnConfirmPickup.setOnClickListener(v -> {
                if (listener != null) listener.onConfirmPickup(order);
            });
        }
    }
}
