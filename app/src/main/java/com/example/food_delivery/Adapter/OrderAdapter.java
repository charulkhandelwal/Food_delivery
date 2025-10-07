package com.example.food_delivery.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_delivery.Model.OrderModel;
import com.example.food_delivery.R;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onConfirmPickupClick(OrderModel order); // Corrected listener for Confirm Pickup
    }

    private List<OrderModel> orders;
    private OnOrderClickListener listener;

    public OrderAdapter(List<OrderModel> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.OrderViewHolder holder, int position) {
        OrderModel order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvItems, tvPrice, tvAddress, btnConfirmPickup;
        LinearLayout layout;
        ImageView imageView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvorderid);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvItems = itemView.findViewById(R.id.tvitems);
            tvPrice = itemView.findViewById(R.id.tvprice);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            layout = itemView.findViewById(R.id.layoutdetails);
            imageView = itemView.findViewById(R.id.ivexpandIcon);
            btnConfirmPickup = itemView.findViewById(R.id.btnConfirmPickup);
        }

        public void bind(final OrderModel order, final OnOrderClickListener listener) {
            // Order ID
            tvOrderId.setText(order.getOrderId() != null && !order.getOrderId().isEmpty()
                    ? "Order #" + order.getOrderId()
                    : "Order");

            tvCustomerName.setText(order.getCustomerName());
            tvItems.setText(order.getItems());
            tvPrice.setText(order.getPrice());
            tvAddress.setText(!order.getAddress().isEmpty() ? "📍 " + order.getAddress() : "No address selected yet");

            // Expand / Collapse
            layout.setVisibility(order.isExpanded() ? View.VISIBLE : View.GONE);
            imageView.setRotation(order.isExpanded() ? 180f : 0f);

            // Click on item to expand/collapse
            itemView.setOnClickListener(v -> {
                order.setExpanded(!order.isExpanded());
                notifyItemChanged(getAdapterPosition());
            });

            // Confirm Pickup Button click
            btnConfirmPickup.setOnClickListener(v -> {
                if (listener != null) listener.onConfirmPickupClick(order);
            });
        }
    }
}
