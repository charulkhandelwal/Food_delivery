package com.example.food_delivery.Model;
import java.util.List;

public class WalletSettleResponse {

    private boolean success;
    private String message;
    private WalletSettleResults results;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public WalletSettleResults getResults() { return results; }

    public static class WalletSettleResults {
        private String _id;
        private double totalAmount;
        private double deliveryManCommission;
        private List<WalletTransaction> allData;
        private double orderAmount;

        public String get_id() { return _id; }
        public double getTotalAmount() { return totalAmount; }
        public double getDeliveryManCommission() { return deliveryManCommission; }
        public List<WalletTransaction> getAllData() { return allData; }
        public double getOrderAmount() { return orderAmount; }
    }

    public static class WalletTransaction {
        private String _id;
        private double transactionAmount;
        private Double driverCommission;
        private String transactionId;
        private String type;
        private String paymentType;
        private String transactionType;
        private String currency;
        private String deliveryManId;
        private String restaurantId;
        private String orderObjectId;
        private String orderId;
        private boolean isCancelled;
        private boolean paidToAdmin;
        private boolean paymentSettled;
        private String createdAt;
        private String updatedAt;
        private int __v;
        private OrderData orderData;

        public String get_id() { return _id; }
        public double getTransactionAmount() { return transactionAmount; }
        public Double getDriverCommission() { return driverCommission; }
        public String getTransactionId() { return transactionId; }
        public String getType() { return type; }
        public String getPaymentType() { return paymentType; }
        public String getTransactionType() { return transactionType; }
        public String getCurrency() { return currency; }
        public String getDeliveryManId() { return deliveryManId; }
        public String getRestaurantId() { return restaurantId; }
        public String getOrderObjectId() { return orderObjectId; }
        public String getOrderId() { return orderId; }
        public boolean isCancelled() { return isCancelled; }
        public boolean isPaidToAdmin() { return paidToAdmin; }
        public boolean isPaymentSettled() { return paymentSettled; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public int get__v() { return __v; }
        public OrderData getOrderData() { return orderData; }
    }

    public static class OrderData {
        private String _id;
        private String userId;
        private String restaurantId;
        private String deliveryManId;
        private List<Dish> dishes;
        private double totalPrice;
        private String discountId;
        private double discountAmount;
        private double finalPrice;
        private Address address;
        private String addressId;
        private String orderId;
        private List<String> deliveryRejectedBy;
        private TrackOrder trackOrder;
        private String status;
        private boolean driverReachedRestaurant;
        private String paymentStatus;
        private String paymentMethod;
        private String createdAt;
        private String updatedAt;
        private int __v;
        private List<DishData> dishesData;

        public String get_id() { return _id; }
        public String getUserId() { return userId; }
        public String getRestaurantId() { return restaurantId; }
        public String getDeliveryManId() { return deliveryManId; }
        public List<Dish> getDishes() { return dishes; }
        public double getTotalPrice() { return totalPrice; }
        public String getDiscountId() { return discountId; }
        public double getDiscountAmount() { return discountAmount; }
        public double getFinalPrice() { return finalPrice; }
        public Address getAddress() { return address; }
        public String getAddressId() { return addressId; }
        public String getOrderId() { return orderId; }
        public List<String> getDeliveryRejectedBy() { return deliveryRejectedBy; }
        public TrackOrder getTrackOrder() { return trackOrder; }
        public String getStatus() { return status; }
        public boolean isDriverReachedRestaurant() { return driverReachedRestaurant; }
        public String getPaymentStatus() { return paymentStatus; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public int get__v() { return __v; }
        public List<DishData> getDishesData() { return dishesData; }
    }

    public static class Dish {
        private String dishId;
        private int quantity;
        private String specialInstructions;
        private double price;
        private String _id;
        private String name;

        public String getDishId() { return dishId; }
        public int getQuantity() { return quantity; }
        public String getSpecialInstructions() { return specialInstructions; }
        public double getPrice() { return price; }
        public String get_id() { return _id; }
        public String getName() { return name; }
    }

    public static class Address {
        private String street;
        private String city;
        private String pincode;

        public String getStreet() { return street; }
        public String getCity() { return city; }
        public String getPincode() { return pincode; }
    }

    public static class TrackOrder {
        private boolean orderAccepted;
        private boolean orderPrepared;
        private boolean orderPickedUp;
        private boolean orderDelivered;

        public boolean isOrderAccepted() { return orderAccepted; }
        public boolean isOrderPrepared() { return orderPrepared; }
        public boolean isOrderPickedUp() { return orderPickedUp; }
        public boolean isOrderDelivered() { return orderDelivered; }
    }

    public static class DishData {
        private String _id;
        private String name;

        public String get_id() { return _id; }
        public String getName() { return name; }
    }
}
