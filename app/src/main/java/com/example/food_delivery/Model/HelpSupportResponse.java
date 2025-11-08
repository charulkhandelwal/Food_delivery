package com.example.food_delivery.Model;

import java.util.Date;

public class HelpSupportResponse {

    private boolean success;
    private String message;
    private Results results;

    public static class Results {
        private String userId;
        private String description;
        private String type;
        private String status;
        private String _id;
        private Date createdAt;
        private Date updatedAt;
        private int __v;

        public String getUserId() { return userId; }
        public String getDescription() { return description; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public String get_id() { return _id; }
        public Date getCreatedAt() { return createdAt; }
        public Date getUpdatedAt() { return updatedAt; }
        public int get__v() { return __v; }
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Results getResults() { return results; }
}
