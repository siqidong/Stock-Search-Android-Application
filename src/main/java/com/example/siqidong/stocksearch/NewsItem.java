package com.example.siqidong.stocksearch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;


public class NewsItem {

    private String title;
    private String link;
    private String content;
    private String publisher;
    private String date;

    public NewsItem(String title, String link, String content, String publisher, String date) {
        super();
        this.title = title;
        this.link = link;
        this.content = content;
        this.publisher = publisher;
        this.date = date;
    }

    public String getTitle() { return title; }

    public String getLink() { return link; }

    public String getContent() {
        return content;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getDate() {
        String oldDate = date;
        String newDate="";

        SimpleDateFormat existingUTCFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat requiredFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");

        try{
            Date getDate = existingUTCFormat.parse(oldDate);
            newDate = requiredFormat.format(getDate);
        }
        catch(ParseException e){
        }

        return "Date: "+newDate;
    }
}
