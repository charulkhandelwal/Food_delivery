package com.example.food_delivery.Model;

public class WalletHistoryModel {
    private String userName;
    private String userAddress;
    private  String userRestaurent;

    private String items;
    private String orderTime;
    private String paymentMode;
    private String transactionId;
    private String amount;


    public WalletHistoryModel(String userName, String userAddress,String userRestaurent, String items,
                              String orderTime, String paymentMode,
                              String transactionId, String amount) {
        this.userName = userName;
        this.userAddress = userAddress;
        this.userRestaurent=userRestaurent;
        this.items = items;
        this.orderTime = orderTime;
        this.paymentMode = paymentMode;
        this.transactionId = transactionId;
        this.amount = amount;
    }

    public String getUserName() { return userName; }
    public String getUserAddress() { return userAddress; }
    public String getItems() { return items; }
    public  String getUserRestaurent(){ return userRestaurent; }
    public String getOrderTime() { return orderTime; }
    public String getPaymentMode() { return paymentMode; }
    public String getTransactionId() { return transactionId; }
    public String getAmount() { return amount; }
}
