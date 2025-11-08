package com.example.food_delivery.Model;

public class DocumentModel {

    private String docName;
    private String imageUri;
    private boolean verified;
    private String docType;

    public DocumentModel(String docName, String imageUri) {
        this.docName = docName;
        this.imageUri = imageUri;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }


    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }
}
