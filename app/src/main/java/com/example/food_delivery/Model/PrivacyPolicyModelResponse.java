package com.example.food_delivery.Model;

public class PrivacyPolicyModelResponse {
    public boolean success;
    public String message;
    public Results results;

    public static class Results {
        public String _id;
        public String title;
        public String description;
        public String slug;
    }
}
