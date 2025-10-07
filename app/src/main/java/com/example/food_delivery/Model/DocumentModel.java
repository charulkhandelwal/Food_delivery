package com.example.food_delivery.Model;

public class DocumentModel {
    private String docName;
    private String imageUri;

    public DocumentModel(String docName, String imageUri) {
        this.docName = docName;
        this.imageUri = imageUri;
    }

    public String getDocName() { return docName; }
    public void setDocName(String docName) { this.docName = docName; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
