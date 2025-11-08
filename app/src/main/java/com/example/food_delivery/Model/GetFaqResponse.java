package com.example.food_delivery.Model;

import java.util.ArrayList;

public class GetFaqResponse {
    public boolean success;
    public String message;
    public ArrayList<Result> results;

    public static class Result {
        public String _id;
        public String title;
        public String content;
    }

    @Override
    public String toString() {
        return "GetFaqResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", results=" + results +
                '}';
    }
}
