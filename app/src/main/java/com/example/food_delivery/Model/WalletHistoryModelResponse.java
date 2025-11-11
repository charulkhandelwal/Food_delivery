package com.example.food_delivery.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WalletHistoryModelResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("results")
    private Results results;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Results getResults() {
        return results;
    }

    // ✅ Inner class for "results"
    public static class Results {
        @SerializedName("docs")
        private List<WalletDoc> docs;

        @SerializedName("totalDocs")
        private int totalDocs;

        @SerializedName("limit")
        private int limit;

        @SerializedName("page")
        private int page;

        @SerializedName("totalPages")
        private int totalPages;

        public List<WalletDoc> getDocs() {
            return docs;
        }

        public int getTotalDocs() {
            return totalDocs;
        }

        public int getLimit() {
            return limit;
        }

        public int getPage() {
            return page;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }

    // ✅ Inner class for each wallet transaction (inside "docs")
    public static class WalletDoc {
        @SerializedName("_id")
        private String id;

        @SerializedName("transactionAmount")
        private double transactionAmount;

        @SerializedName("driverCommission")
        private double driverCommission;

        @SerializedName("transactionId")
        private String transactionId;

        @SerializedName("type")
        private String type;

        @SerializedName("paymentType")
        private String paymentType;

        @SerializedName("transactionType")
        private String transactionType;

        @SerializedName("currency")
        private String currency;

        @SerializedName("orderId")
        private String orderId;

        @SerializedName("isCancelled")
        private boolean isCancelled;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("orderData")
        private OrderData orderData;

        public String getId() { return id; }
        public double getTransactionAmount() { return transactionAmount; }
        public double getDriverCommission() { return driverCommission; }
        public String getTransactionId() { return transactionId; }
        public String getType() { return type; }
        public String getPaymentType() { return paymentType; }
        public String getTransactionType() { return transactionType; }
        public String getCurrency() { return currency; }
        public String getOrderId() { return orderId; }
        public boolean isCancelled() { return isCancelled; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public OrderData getOrderData() { return orderData; }
    }

    // ✅ Inner class for "orderData"
    public static class OrderData {
        @SerializedName("_id")
        private String id;

        @SerializedName("orderId")
        private String orderId;

        @SerializedName("totalPrice")
        private double totalPrice;

        @SerializedName("finalPrice")
        private double finalPrice;

        @SerializedName("status")
        private String status;

        @SerializedName("paymentStatus")
        private String paymentStatus;

        @SerializedName("paymentMethod")
        private String paymentMethod;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("restaurantData")
        private RestaurantData restaurantData;

        @SerializedName("userData")
        private UserData userData;

        @SerializedName("dishes")
        private List<Dish> dishes;

        public String getId() { return id; }
        public String getOrderId() { return orderId; }
        public double getTotalPrice() { return totalPrice; }
        public double getFinalPrice() { return finalPrice; }
        public String getStatus() { return status; }
        public String getPaymentStatus() { return paymentStatus; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public RestaurantData getRestaurantData() { return restaurantData; }
        public UserData getUserData() { return userData; }
        public List<Dish> getDishes() { return dishes; }
    }

    // ✅ Inner class for "restaurantData"
    public static class RestaurantData {
        @SerializedName("_id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("address")
        private String address;

        @SerializedName("phone")
        private String phone;

        @SerializedName("email")
        private String email;

        @SerializedName("restaurantLogo")
        private String restaurantLogo;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getRestaurantLogo() { return restaurantLogo; }
    }

    // ✅ Inner class for "userData"
    public static class UserData {
        @SerializedName("_id")
        private String id;

        @SerializedName("firstName")
        private String firstName;

        @SerializedName("lastName")
        private String lastName;

        @SerializedName("email")
        private String email;

        @SerializedName("mobile")
        private String mobile;

        public String getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getMobile() { return mobile; }
    }

    // ✅ Inner class for "dishes"
    public static class Dish {
        @SerializedName("dishId")
        private String dishId;

        @SerializedName("name")
        private String name;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("price")
        private double price;

        @SerializedName("specialInstructions")
        private String specialInstructions;

        public String getDishId() { return dishId; }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public String getSpecialInstructions() { return specialInstructions; }
    }
}
