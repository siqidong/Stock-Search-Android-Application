package com.example.siqidong.stocksearch;

public class CurrentItem {

    private String title = "";
    private String description = "";

    public CurrentItem(String title, String description) {
        super();
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}