package com.example.food_delivery.Model;

import java.util.List;

public class CodSettelmentModel {

    private String restaurantName;
    private String orderId;
    private String paymentMethod;
    private double totalAmount;
    private double commission;
    private List<String> dishes; // Dishes list

    public CodSettelmentModel (String restaurantName, String orderId, String paymentMethod, double totalAmount, double commission, List<String> dishes) {
        this.restaurantName = restaurantName;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.commission = commission;
        this.dishes = dishes;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getCommission() {
        return commission;
    }

    public List<String> getDishes() {
        return dishes;
    }
}
