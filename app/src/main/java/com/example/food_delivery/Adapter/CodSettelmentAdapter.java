package com.example.food_delivery.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_delivery.Model.CodSettelmentModel;
import com.example.food_delivery.R;

import java.util.List;

public class CodSettelmentAdapter extends RecyclerView.Adapter<CodSettelmentAdapter.ViewHolder> {

    private final Context context;
    private final List<CodSettelmentModel> codList;

    public CodSettelmentAdapter(Context context, List<CodSettelmentModel> codList) {
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
        CodSettelmentModel model = codList.get(position);

        holder.tvRestaurantName.setText(model.getRestaurantName());
        holder.tvOrderId.setText("Order ID: #" + model.getOrderId());
        holder.tvPaymentMethod.setText("Payment Method: " + model.getPaymentMethod());

        // ✅ Dishes ko dynamically add karne ke liye container clear karo pehle
        holder.itemContainer.removeAllViews();

        /*
         *  ✅ Yahan pe tumhe API se aayi hui dish list milti hai (model.getDishes())
         *  Ab us list ko iterate karke TextView add kar dena:
         */
        if (model.getDishes() != null && !model.getDishes().isEmpty()) {
            for (String dishName : model.getDishes()) {
                TextView tvDish = new TextView(context);
                tvDish.setText("• " + dishName);
                tvDish.setTextSize(14);
                tvDish.setTextColor(context.getResources().getColor(R.color.black));
                tvDish.setPadding(8, 4, 8, 4);
                holder.itemContainer.addView(tvDish);
            }
        } else {
            // Agar dishes list empty hai to placeholder text dikhao
            TextView noDish = new TextView(context);
            noDish.setText("No dishes found");
            noDish.setTextColor(context.getResources().getColor(R.color.lightpink));
            noDish.setPadding(8, 4, 8, 4);
            holder.itemContainer.addView(noDish);
        }

        // NOTE:
        // Agar baad mein API se data load karoge to bas notifyDataSetChanged() call karna
        // aur modelList update karna
    }

    @Override
    public int getItemCount() {
        return codList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurantName, tvOrderId, tvPaymentMethod;
        LinearLayout itemContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            itemContainer = itemView.findViewById(R.id.itemContainer);
        }
    }
}
