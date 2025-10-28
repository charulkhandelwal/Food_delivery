package com.example.food_delivery.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DocumentGetResponse {

    private boolean success;
    private String message;
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

    // ✅ Helper method to convert backend response to list of DocumentModel
    public List<DocumentModel> getDocuments() {
        List<DocumentModel> list = new ArrayList<>();

        if (results != null && results.documents != null) {
            Documents docs = results.documents;

            // Aadhar
            if (docs.aadhar != null) {
                DocumentModel aadhar = new DocumentModel("Aadhar Card", docs.aadhar.front);
                aadhar.setDocType("aadhar");
                aadhar.setVerified("verified".equalsIgnoreCase(safeString(docs.aadhar.status)));
                list.add(aadhar);
            }

            // PAN
            if (docs.pan != null) {
                DocumentModel pan = new DocumentModel("PAN Card", "");
                pan.setDocType("pan");
                pan.setVerified("verified".equalsIgnoreCase(safeString(docs.pan.status)));
                list.add(pan);
            }

            // Driving Licence
            if (docs.drivingLicence != null) {
                DocumentModel dl = new DocumentModel("Driving Licence", "");
                dl.setDocType("drivingLicence");
                dl.setVerified("verified".equalsIgnoreCase(safeString(docs.drivingLicence.status)));
                list.add(dl);
            }

            // RC
            if (docs.rc != null) {
                DocumentModel rc = new DocumentModel("RC", "");
                rc.setDocType("rc");
                rc.setVerified("verified".equalsIgnoreCase(safeString(docs.rc.status)));
                list.add(rc);
            }

            // Bank Account
            if (docs.bankAccountDetails != null) {
                DocumentModel bank = new DocumentModel("Bank Account", "");
                bank.setDocType("bank");
                bank.setVerified("verified".equalsIgnoreCase(safeString(docs.bankAccountDetails.status)));
                list.add(bank);
            }
        }

        return list;
    }

    // ✅ Helper method to prevent NullPointerException
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    // ===============================
    // 🔹 Inner classes (matching backend JSON)
    // ===============================

    public static class Results {
        public Documents documents;
        public String _id;
        public String deliveryPartnerId;
        public Date createdAt;
        public Date updatedAt;
    }

    public static class Documents {
        public Aadhar aadhar;
        public Pan pan;
        public DrivingLicence drivingLicence;
        public Rc rc;
        public BankAccountDetails bankAccountDetails;
    }

    public static class Aadhar {
        public String back;
        public String front;
        public String status;
    }

    public static class Pan {
        public String status;
    }

    public static class DrivingLicence {
        public String status;
    }

    public static class Rc {
        public String status;
    }

    public static class BankAccountDetails {
        public String status;
    }
}
