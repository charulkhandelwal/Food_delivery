package com.example.food_delivery.Model;

import java.util.ArrayList;
import java.util.Date;

public class ProfileModel {
    public boolean success;
    public String message;
    public Results results;

    public static class Results {
        public Location location;
        public String _id;
        public String mobile;
        public ArrayList<String> languages;
        public boolean isAvailable;
        public Date createdAt;
        public Date updatedAt;
        public Object otp;
        public Object otpExpires;
        public String token;
        public String address;
        public String bloodGroup;
        public String city;
        public String dob;
        public String fatherName;
        public String firstName;
        public String lastName;
        public String primaryMobile;
        public String profile;
        public String secondaryMobile;
    }

    public static class Location {
        public String type;
        public ArrayList<Double> coordinates;
    }
}
