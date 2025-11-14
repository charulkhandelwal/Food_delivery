package com.example.food_delivery.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_delivery.Model.WalletSettleResponse;
import com.example.food_delivery.R;

import java.util.List;

public class CodSettelmentAdapter extends RecyclerView.Adapter<CodSettelmentAdapter.ViewHolder> {

    private final Context context;
    private final List<WalletSettleResponse.WalletTransaction> codList;

    public CodSettelmentAdapter(Context context, List<WalletSettleResponse.WalletTransaction> codList) {
        this.context = context;
        this.codList = codList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cod_settelment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WalletSettleResponse.WalletTransaction model = codList.get(position);
        WalletSettleResponse.OrderData orderData = model.getOrderData();

        // ✅ Order ID
        holder.tvOrderId.setText("Order ID: #" + model.getOrderId());

        // ✅ Payment Method
        if (orderData != null && orderData.getPaymentMethod() != null) {
            holder.tvPaymentMethod.setText("Payment Method: " + orderData.getPaymentMethod());
        } else {
            holder.tvPaymentMethod.setText("Payment Method: N/A");
        }

        holder.tvTotalPayment.setText("Total Payment: " + orderData.getTotalPrice());

       /* // ✅ Restaurant Name
        if (orderData != null && orderData.getRestaurantId() != null) {
            holder.tvRestaurantName.setText("Restaurant ID: " + orderData.getRestaurantId());
        } else {
            holder.tvRestaurantName.setText("Restaurant: Unknown");
        }
*/
        // ✅ Restaurant Name
        if (orderData != null && orderData.getRestaurantId() != null) {
            holder.tvRestaurantId.setText("Restaurant ID: " + orderData.getRestaurantId());
        } else {
            holder.tvRestaurantId.setText("Restaurant: Unknown");
        }

        // ✅ Clear container before adding new views
        holder.itemContainer.removeAllViews();

        // ✅ Add dish list dynamically
        if (orderData != null && orderData.getDishes() != null && !orderData.getDishes().isEmpty()) {
            for (WalletSettleResponse.Dish dish : orderData.getDishes()) {
                TextView tvDish = new TextView(context);
                tvDish.setText("• " + dish.getName() + " × " + dish.getQuantity() + " — ₹" + dish.getPrice());
                tvDish.setTextSize(14);
                tvDish.setTextColor(context.getResources().getColor(R.color.black));
                tvDish.setPadding(8, 4, 8, 4);
                holder.itemContainer.addView(tvDish);
            }
        } else {
            // Show placeholder if no dishes
            TextView noDish = new TextView(context);
            noDish.setText("No dishes found");
            noDish.setTextColor(context.getResources().getColor(R.color.lightpink));
            noDish.setPadding(8, 4, 8, 4);
            holder.itemContainer.addView(noDish);
        }
    }

    @Override
    public int getItemCount() {
        return codList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurantName, tvOrderId, tvPaymentMethod,tvRestaurantId,tvTotalPayment;
        LinearLayout itemContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvRestaurantId = itemView.findViewById(R.id.tvRestaurantId);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            itemContainer = itemView.findViewById(R.id.itemContainer);
            tvTotalPayment = itemView.findViewById(R.id.tvTotalPayment);
        }
    }
}
