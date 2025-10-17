package com.example.food_delivery.Model;

public class OtpVerifyResponse {

    public boolean success;
    public String message;
    public Results results;

    // Nested Results class
    public static class Results {
        public String token;
        public Partner partner;
        public boolean isNewUser;
    }

    // Nested Partner class
    public static class Partner {
        public String id;
        public String mobile;
        public String role;
    }
}
