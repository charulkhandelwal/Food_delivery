package com.example.food_delivery.Model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class DocumentResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("results")
    private Results results;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Results getResults() {
        return results;
    }

    // --------------------------- Inner classes ---------------------------

    public static class Results {
        @SerializedName("documents")
        private Documents documents;

        @SerializedName("_id")
        private String id;

        @SerializedName("deliveryPartnerId")
        private String deliveryPartnerId;

        @SerializedName("createdAt")
        private Date createdAt;

        @SerializedName("updatedAt")
        private Date updatedAt;

        public Documents getDocuments() { return documents; }
        public String getId() { return id; }
        public String getDeliveryPartnerId() { return deliveryPartnerId; }
        public Date getCreatedAt() { return createdAt; }
        public Date getUpdatedAt() { return updatedAt; }
    }

    public static class Documents {
        @SerializedName("aadhar")
        private Aadhar aadhar;
        @SerializedName("pan")
        private Pan pan;
        @SerializedName("drivingLicence")
        private DrivingLicence drivingLicence;
        @SerializedName("rc")
        private Rc rc;
        @SerializedName("bankAccountDetails")
        private BankAccountDetails bankAccountDetails;

        public Aadhar getAadhar() { return aadhar; }
        public Pan getPan() { return pan; }
        public DrivingLicence getDrivingLicence() { return drivingLicence; }
        public Rc getRc() { return rc; }
        public BankAccountDetails getBankAccountDetails() { return bankAccountDetails; }
    }

    public static class Aadhar {
        @SerializedName("status")
        private String status;
        @SerializedName("front")
        private String front;
        @SerializedName("back")
        private String back;

        public String getStatus() { return status; }
        public String getFront() { return front; }
        public String getBack() { return back; }
    }

    public static class Pan {
        @SerializedName("status")
        private String status;
        @SerializedName("front")
        private String front;

        public String getStatus() { return status; }
        public String getFront() { return front; }
    }

    public static class DrivingLicence {
        @SerializedName("status")
        private String status;

        public String getStatus() { return status; }
    }

    public static class Rc {
        @SerializedName("status")
        private String status;
        @SerializedName("front")
        private String front;
        @SerializedName("back")
        private String back;

        public String getStatus() { return status; }
        public String getFront() { return front; }
        public String getBack() { return back; }
    }

    public static class BankAccountDetails {
        @SerializedName("status")
        private String status;
        @SerializedName("accountNumber")
        private int accountNumber;
        @SerializedName("ifscCode")
        private String ifscCode;
        @SerializedName("name")
        private String name;

        public String getStatus() { return status; }
        public int getAccountNumber() { return accountNumber; }
        public String getIfscCode() { return ifscCode; }
        public String getName() { return name; }
    }
}
