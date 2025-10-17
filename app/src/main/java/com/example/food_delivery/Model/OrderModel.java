package com.example.food_delivery.Model;

import java.util.List;

public class OrderModel {

    private boolean success;
    private String message;
    private List<ResultsBean> results;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<ResultsBean> getResults() { return results; }
    public void setResults(List<ResultsBean> results) { this.results = results; }

    public static class ResultsBean {
        private String _id;
        private String userId;
        private String restaurantId;
        private Object deliveryManId;
        private int totalPrice;
        private Object discountId;
        private int discountAmount;
        private String finalPrice;
        private AddressBean address;
        private String addressId;
        private String orderId;
        private String status;
        private String paymentStatus;
        private String paymentMethod;
        private StripePaymentBean stripePayment;
        private String createdAt;
        private String updatedAt;
        private int __v;
        private RestaurantDataBean restaurantData;
        private UserDataBean userData;
        private List<DishesBean> dishes;

        // ✅ Local field (not part of API)
        private transient boolean isNew = true;
        private transient boolean isExpanded = false;

        public boolean isNew() {
            return isNew;
        }

        public void setNew(boolean isNew) {
            this.isNew = isNew;
        }
        public boolean isExpanded() {
            return isExpanded;
        }

        public void setExpanded(boolean expanded) {
            this.isExpanded = expanded;
        }

        public String get_id() { return _id; }
        public void set_id(String _id) { this._id = _id; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getRestaurantId() { return restaurantId; }
        public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

        public Object getDeliveryManId() { return deliveryManId; }
        public void setDeliveryManId(Object deliveryManId) { this.deliveryManId = deliveryManId; }

        public int getTotalPrice() { return totalPrice; }
        public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }

        public Object getDiscountId() { return discountId; }
        public void setDiscountId(Object discountId) { this.discountId = discountId; }

        public int getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(int discountAmount) { this.discountAmount = discountAmount; }

        public String getFinalPrice() { return finalPrice; }
        public void setFinalPrice(String finalPrice) { this.finalPrice = finalPrice; }

        public AddressBean getAddress() { return address; }
        public void setAddress(AddressBean address) { this.address = address; }

        public String getAddressId() { return addressId; }
        public void setAddressId(String addressId) { this.addressId = addressId; }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public StripePaymentBean getStripePayment() { return stripePayment; }
        public void setStripePayment(StripePaymentBean stripePayment) { this.stripePayment = stripePayment; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        public int get__v() { return __v; }
        public void set__v(int __v) { this.__v = __v; }

        public RestaurantDataBean getRestaurantData() { return restaurantData; }
        public void setRestaurantData(RestaurantDataBean restaurantData) { this.restaurantData = restaurantData; }

        public UserDataBean getUserData() { return userData; }
        public void setUserData(UserDataBean userData) { this.userData = userData; }

        public List<DishesBean> getDishes() { return dishes; }
        public void setDishes(List<DishesBean> dishes) { this.dishes = dishes; }

        public static class AddressBean {
            private String street;
            private String city;
            private String pincode;

            public String getStreet() { return street; }
            public void setStreet(String street) { this.street = street; }

            public String getCity() { return city; }
            public void setCity(String city) { this.city = city; }

            public String getPincode() { return pincode; }
            public void setPincode(String pincode) { this.pincode = pincode; }
        }

        public static class StripePaymentBean {
            private String id;
            private int amount;
            private String currency;
            private String status;
            private String payment_method;
            private int created;

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }

            public int getAmount() { return amount; }
            public void setAmount(int amount) { this.amount = amount; }

            public String getCurrency() { return currency; }
            public void setCurrency(String currency) { this.currency = currency; }

            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }

            public String getPayment_method() { return payment_method; }
            public void setPayment_method(String payment_method) { this.payment_method = payment_method; }

            public int getCreated() { return created; }
            public void setCreated(int created) { this.created = created; }
        }

        public static class RestaurantDataBean {
            private String _id;
            private String name;
            private String address;
            private int maxDeliveryTime;
            private int minDeliveryTime;
            private String zone;
            private AddresslatLngBean addresslatLng;
            private String ownerFirstName;
            private String ownerFullName;
            private String phone;
            private String fssaiLicenseNumber;
            private String tinNumber;
            private String email;
            private String password;
            private String status;
            private String registrationStatus;
            private String deviceId;
            private String deviceType;
            private String deviceToken;
            private boolean notifications;
            private boolean forceLogout;
            private String countryCode;
            private String rejectionReason;
            private boolean isEmailVerified;
            private boolean isMobileVerified;
            private String role;
            private String createdAt;
            private String updatedAt;
            private int __v;
            private String restaurantLogo;
            private List<String> cuisine;
            private List<String> tags;

            public String get_id() { return _id; }
            public void set_id(String _id) { this._id = _id; }

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }

            public String getAddress() { return address; }
            public void setAddress(String address) { this.address = address; }

            public int getMaxDeliveryTime() { return maxDeliveryTime; }
            public void setMaxDeliveryTime(int maxDeliveryTime) { this.maxDeliveryTime = maxDeliveryTime; }

            public int getMinDeliveryTime() { return minDeliveryTime; }
            public void setMinDeliveryTime(int minDeliveryTime) { this.minDeliveryTime = minDeliveryTime; }

            public String getZone() { return zone; }
            public void setZone(String zone) { this.zone = zone; }

            public AddresslatLngBean getAddresslatLng() { return addresslatLng; }
            public void setAddresslatLng(AddresslatLngBean addresslatLng) { this.addresslatLng = addresslatLng; }

            public String getOwnerFirstName() { return ownerFirstName; }
            public void setOwnerFirstName(String ownerFirstName) { this.ownerFirstName = ownerFirstName; }

            public String getOwnerFullName() { return ownerFullName; }
            public void setOwnerFullName(String ownerFullName) { this.ownerFullName = ownerFullName; }

            public String getPhone() { return phone; }
            public void setPhone(String phone) { this.phone = phone; }

            public String getFssaiLicenseNumber() { return fssaiLicenseNumber; }
            public void setFssaiLicenseNumber(String fssaiLicenseNumber) { this.fssaiLicenseNumber = fssaiLicenseNumber; }

            public String getTinNumber() { return tinNumber; }
            public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }

            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }

            public String getPassword() { return password; }
            public void setPassword(String password) { this.password = password; }

            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }

            public String getRegistrationStatus() { return registrationStatus; }
            public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }

            public String getDeviceId() { return deviceId; }
            public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

            public String getDeviceType() { return deviceType; }
            public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

            public String getDeviceToken() { return deviceToken; }
            public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }

            public boolean isNotifications() { return notifications; }
            public void setNotifications(boolean notifications) { this.notifications = notifications; }

            public boolean isForceLogout() { return forceLogout; }
            public void setForceLogout(boolean forceLogout) { this.forceLogout = forceLogout; }

            public String getCountryCode() { return countryCode; }
            public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

            public String getRejectionReason() { return rejectionReason; }
            public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

            public boolean isIsEmailVerified() { return isEmailVerified; }
            public void setIsEmailVerified(boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

            public boolean isIsMobileVerified() { return isMobileVerified; }
            public void setIsMobileVerified(boolean isMobileVerified) { this.isMobileVerified = isMobileVerified; }

            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }

            public String getCreatedAt() { return createdAt; }
            public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

            public String getUpdatedAt() { return updatedAt; }
            public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

            public int get__v() { return __v; }
            public void set__v(int __v) { this.__v = __v; }

            public String getRestaurantLogo() { return restaurantLogo; }
            public void setRestaurantLogo(String restaurantLogo) { this.restaurantLogo = restaurantLogo; }

            public List<String> getCuisine() { return cuisine; }
            public void setCuisine(List<String> cuisine) { this.cuisine = cuisine; }

            public List<String> getTags() { return tags; }
            public void setTags(List<String> tags) { this.tags = tags; }

            public static class AddresslatLngBean {
                private String type;
                private List<Double> coordinates;

                public String getType() { return type; }
                public void setType(String type) { this.type = type; }

                public List<Double> getCoordinates() { return coordinates; }
                public void setCoordinates(List<Double> coordinates) { this.coordinates = coordinates; }
            }
        }

        public static class UserDataBean {
            private String _id;
            private String firstName;
            private String lastName;
            private String fullName;
            private String email;
            private String mobile;
            private String role;
            private boolean isMobileVerified;
            private boolean isEmailVerified;
            private boolean newUserVerified;
            private String status;
            private boolean isDeleted;
            private Object deviceId;
            private Object deviceType;
            private Object deviceToken;
            private String registrationType;
            private Object rating;
            private Object socialId;
            private boolean notifications;
            private boolean forceLogout;
            private String createdAt;
            private String updatedAt;
            private int __v;
            private String gender;
            private String profilePic;
            private String dob;
            private String countryCode;
            private List<AddressesBean> addresses;

            public String get_id() { return _id; }
            public void set_id(String _id) { this._id = _id; }

            public String getFirstName() { return firstName; }
            public void setFirstName(String firstName) { this.firstName = firstName; }

            public String getLastName() { return lastName; }
            public void setLastName(String lastName) { this.lastName = lastName; }

            public String getFullName() { return fullName; }
            public void setFullName(String fullName) { this.fullName = fullName; }

            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }

            public String getMobile() { return mobile; }
            public void setMobile(String mobile) { this.mobile = mobile; }

            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }

            public boolean isIsMobileVerified() { return isMobileVerified; }
            public void setIsMobileVerified(boolean isMobileVerified) { this.isMobileVerified = isMobileVerified; }

            public boolean isIsEmailVerified() { return isEmailVerified; }
            public void setIsEmailVerified(boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

            public boolean isNewUserVerified() { return newUserVerified; }
            public void setNewUserVerified(boolean newUserVerified) { this.newUserVerified = newUserVerified; }

            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }

            public boolean isIsDeleted() { return isDeleted; }
            public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

            public Object getDeviceId() { return deviceId; }
            public void setDeviceId(Object deviceId) { this.deviceId = deviceId; }

            public Object getDeviceType() { return deviceType; }
            public void setDeviceType(Object deviceType) { this.deviceType = deviceType; }

            public Object getDeviceToken() { return deviceToken; }
            public void setDeviceToken(Object deviceToken) { this.deviceToken = deviceToken; }

            public String getRegistrationType() { return registrationType; }
            public void setRegistrationType(String registrationType) { this.registrationType = registrationType; }

            public Object getRating() { return rating; }
            public void setRating(Object rating) { this.rating = rating; }

            public Object getSocialId() { return socialId; }
            public void setSocialId(Object socialId) { this.socialId = socialId; }

            public boolean isNotifications() { return notifications; }
            public void setNotifications(boolean notifications) { this.notifications = notifications; }

            public boolean isForceLogout() { return forceLogout; }
            public void setForceLogout(boolean forceLogout) { this.forceLogout = forceLogout; }

            public String getCreatedAt() { return createdAt; }
            public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

            public String getUpdatedAt() { return updatedAt; }
            public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

            public int get__v() { return __v; }
            public void set__v(int __v) { this.__v = __v; }

            public String getGender() { return gender; }
            public void setGender(String gender) { this.gender = gender; }

            public String getProfilePic() { return profilePic; }
            public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

            public String getDob() { return dob; }
            public void setDob(String dob) { this.dob = dob; }

            public String getCountryCode() { return countryCode; }
            public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

            public List<AddressesBean> getAddresses() { return addresses; }
            public void setAddresses(List<AddressesBean> addresses) { this.addresses = addresses; }

            public static class AddressesBean {
                private String _id;
                private String street;
                private String city;
                private String state;
                private String country;
                private String zipCode;
                private LocationObjectBean locationObject;
                private String completeAddress;
                private boolean defaultAddress;
                private String createdAt;
                private String updatedAt;

                public String get_id() { return _id; }
                public void set_id(String _id) { this._id = _id; }

                public String getStreet() { return street; }
                public void setStreet(String street) { this.street = street; }

                public String getCity() { return city; }
                public void setCity(String city) { this.city = city; }

                public String getState() { return state; }
                public void setState(String state) { this.state = state; }

                public String getCountry() { return country; }
                public void setCountry(String country) { this.country = country; }

                public String getZipCode() { return zipCode; }
                public void setZipCode(String zipCode) { this.zipCode = zipCode; }

                public LocationObjectBean getLocationObject() { return locationObject; }
                public void setLocationObject(LocationObjectBean locationObject) { this.locationObject = locationObject; }

                public String getCompleteAddress() { return completeAddress; }
                public void setCompleteAddress(String completeAddress) { this.completeAddress = completeAddress; }

                public boolean isDefaultAddress() { return defaultAddress; }
                public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }

                public String getCreatedAt() { return createdAt; }
                public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

                public String getUpdatedAt() { return updatedAt; }
                public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

                public static class LocationObjectBean {
                    private String type;
                    private List<Double> coordinates;

                    public String getType() { return type; }
                    public void setType(String type) { this.type = type; }

                    public List<Double> getCoordinates() { return coordinates; }
                    public void setCoordinates(List<Double> coordinates) { this.coordinates = coordinates; }
                }
            }
        }

        public static class DishesBean {
            private String _id;
            private String dishId;
            private int quantity;
            private int price;
            private String specialInstructions;

            public String get_id() { return _id; }
            public void set_id(String _id) { this._id = _id; }

            public String getDishId() { return dishId; }
            public void setDishId(String dishId) { this.dishId = dishId; }

            public int getQuantity() { return quantity; }
            public void setQuantity(int quantity) { this.quantity = quantity; }

            public int getPrice() { return price; }
            public void setPrice(int price) { this.price = price; }

            public String getSpecialInstructions() { return specialInstructions; }
            public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
        }


    }
}
