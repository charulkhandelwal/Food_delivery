package com.example.food_delivery.Model;

import com.google.gson.annotations.SerializedName;

public class AcceptRejectOrderModel {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("results")
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

        @SerializedName("activeOrderId")
        private String activeOrderId;

        public String getActiveOrderId() {
            return activeOrderId;
        }

        public void setActiveOrderId(String activeOrderId) {
            this.activeOrderId = activeOrderId;
        }
    }
}
