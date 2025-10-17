package com.example.food_delivery.Model;

public class GetProfileResponse {


    public Results results;

    public class Results{
        public String _id;
        public String mobile;
        public String address;
        public String bloodGroup;
        public String city;
        public String dob;
        public String firstName;
        public String lastName;
        public String profile;
    }

    public class Root{
        public boolean success;
        public String message;
        public Results results;
    }


}
