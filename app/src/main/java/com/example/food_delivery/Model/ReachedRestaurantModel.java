package com.example.food_delivery.Model;

public class ReachedRestaurantModel {
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
        // Empty because "results": {} in the response
    }
}
