package com.example.food_delivery.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_delivery.Model.WalletHistoryModel;
import com.example.food_delivery.databinding.WalletHistoryItemBinding;

import java.util.List;

public class WalletHistoryAdapter extends RecyclerView.Adapter<WalletHistoryAdapter.ViewHolder> {

    private final List<WalletHistoryModel> walletList;

    public WalletHistoryAdapter(List<WalletHistoryModel> walletList) {
        this.walletList = walletList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        WalletHistoryItemBinding binding = WalletHistoryItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WalletHistoryModel model = walletList.get(position);
        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return walletList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final WalletHistoryItemBinding binding;

        public ViewHolder(@NonNull WalletHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(WalletHistoryModel model) {
            binding.tvUserName.setText(model.getUserName());
            binding.tvUserAddress.setText(model.getUserAddress());
            binding.tvUserItems.setText(model.getItems());
            binding.tvOrderTime.setText(model.getOrderTime());
            binding.tvAmount.setText(model.getAmount());
            binding.tvPaymentMode.setText(model.getPaymentMode());
            binding.tvTransactionId.setText(model.getTransactionId());
            binding.tvUserRestaurent.setText(model.getUserRestaurent());
        }
    }
}
