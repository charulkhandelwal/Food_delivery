package com.example.food_delivery.Adapter;

import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.food_delivery.Model.FAQModel;
import com.example.food_delivery.R;

import java.util.List;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    private final List<FAQModel> faqList;

    public FAQAdapter(List<FAQModel> faqList) {
        this.faqList = faqList;
    }

    @Override
    public FAQViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FAQViewHolder holder, int position) {
        FAQModel faq = faqList.get(position);
        holder.tvQuestion.setText(faq.getTitle());
        holder.tvAnswer.setText(faq.getContent());

        boolean isExpanded = faq.isExpanded();
        holder.tvAnswer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.ivArrow.setRotation(isExpanded ? 180 : 0);

        holder.itemView.setOnClickListener(v -> {
            faq.setExpanded(!faq.isExpanded());
            notifyItemChanged(position);
        });

        // Fade animation when showing the answer
        if (isExpanded) {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(200);
            holder.tvAnswer.startAnimation(fadeIn);

            ValueAnimator anim = ValueAnimator.ofInt(0, holder.tvAnswer.getMeasuredHeight());
            anim.setDuration(200);
            anim.start();
        }
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    static class FAQViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer;
        ImageView ivArrow;

        public FAQViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            ivArrow = itemView.findViewById(R.id.imgArrow);
        }
    }
}
