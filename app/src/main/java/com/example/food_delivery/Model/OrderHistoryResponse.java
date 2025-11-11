package com.example.food_delivery.Model;

import java.util.List;

public class OrderHistoryResponse {

    private boolean success;
    private String message;
    private Results results;


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }


    public static class Results {

        private List<Object> docs;
        private int totalDocs;
        private int limit;
        private int page;
        private int totalPages;


        private int totalOrders;
        private int pageSize;
        private boolean hasNextPage;
        private boolean hasPrevPage;
        private List<OrderData> data;


        public List<Object> getDocs() {
            return docs;
        }

        public void setDocs(List<Object> docs) {
            this.docs = docs;
        }

        public int getTotalDocs() {
            return totalDocs;
        }

        public void setTotalDocs(int totalDocs) {
            this.totalDocs = totalDocs;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public int getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public boolean isHasPrevPage() {
            return hasPrevPage;
        }

        public void setHasPrevPage(boolean hasPrevPage) {
            this.hasPrevPage = hasPrevPage;
        }

        public List<OrderData> getData() {
            return data;
        }

        public void setData(List<OrderData> data) {
            this.data = data;
        }
    }

    public static class OrderData {
        private String _id;
        private UserId userId;
        private RestaurantId restaurantId;
        private double finalPrice;
        private AddressId addressId;
        private String orderId;
        private String status;
        private String createdAt;


        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public UserId getUserId() {
            return userId;
        }

        public void setUserId(UserId userId) {
            this.userId = userId;
        }

        public RestaurantId getRestaurantId() {
            return restaurantId;
        }

        public void setRestaurantId(RestaurantId restaurantId) {
            this.restaurantId = restaurantId;
        }

        public double getFinalPrice() {
            return finalPrice;
        }

        public void setFinalPrice(double finalPrice) {
            this.finalPrice = finalPrice;
        }

        public AddressId getAddressId() {
            return addressId;
        }

        public void setAddressId(AddressId addressId) {
            this.addressId = addressId;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }


    public static class UserId {
        private String _id;
        private String firstName;
        private String mobile;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }
    }

    public static class RestaurantId {
        private String _id;
        private String name;
        private String address;
        private String restaurantLogo;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getRestaurantLogo() {
            return restaurantLogo;
        }

        public void setRestaurantLogo(String restaurantLogo) {
            this.restaurantLogo = restaurantLogo;
        }
    }


    public static class AddressId {
        private String _id;
        private String addressType;
        private String zipCode;
        private Location location;
        private String completeAddress;
        private String floor;
        private String area;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getAddressType() {
            return addressType;
        }

        public void setAddressType(String addressType) {
            this.addressType = addressType;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public String getCompleteAddress() {
            return completeAddress;
        }

        public void setCompleteAddress(String completeAddress) {
            this.completeAddress = completeAddress;
        }

        public String getFloor() {
            return floor;
        }

        public void setFloor(String floor) {
            this.floor = floor;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }
    }


    public static class Location {
        private String type;
        private List<Double> coordinates;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Double> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Double> coordinates) {
            this.coordinates = coordinates;
        }
    }
}
