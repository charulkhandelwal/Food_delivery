package com.example.food_delivery.Model;

import java.util.Date;

public class DocumentResponse {

    public class Aadhar{
        public String back;
        public String front;
    }

    public class BankAccountDetails{
        public String accountNumber;
        public String ifscCode;
        public String name;
    }

    public class Documents{
        public Aadhar aadhar;
        public Pan pan;
        public DrivingLicence drivingLicence;
        public Rc rc;
        public BankAccountDetails bankAccountDetails;
    }

    public class DrivingLicence{
        public String back;
        public String front;
    }

    public class Pan{
        public String back;
        public String front;
    }

    public class Rc{
        public String back;
        public String front;
    }

    public class Results{
        public Documents documents;
        public String _id;
        public String deliveryPartnerId;
        public Date createdAt;
        public Date updatedAt;
    }

    public class Root{
        public boolean success;
        public String message;
        public Results results;
    }



}
