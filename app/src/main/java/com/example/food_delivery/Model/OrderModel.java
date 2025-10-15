package com.example.food_delivery.Model;

public class OrderModel {

    private String orderId;
    private String customerName;
    private String items;
    private String price;
    private String address;
    private String status;

    private boolean expanded = false; // existing
    private boolean isNew = false;    // ✅ new field

    public OrderModel(String orderId, String customerName, String items, String price, String address, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.items = items;
        this.price = price;
        this.address = address;
        this.status = status;
    }

    // Existing getters/setters
    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getItems() { return items; }
    public String getPrice() { return price; }
    public String getAddress() { return address; }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() { return status; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }

    // ✅ New getters/setters for isNew
    public boolean isNew() { return isNew; }
    public void setNew(boolean aNew) { this.isNew = aNew; }
}
