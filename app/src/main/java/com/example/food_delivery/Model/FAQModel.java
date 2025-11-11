package com.example.food_delivery.Model;

public class FAQModel {
    private String title;
    private String content;
    private boolean expanded;

    public FAQModel(String title, String content) {
        this.title = title;
        this.content = content;
        this.expanded = false;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
}
