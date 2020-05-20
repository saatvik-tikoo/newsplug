package com.example.newsplug.ui.common;

public class HeadlinesList {
    private String id;
    private String imageurl;
    private String title;
    private String timeTillDate;
    private String newsTag;
    private String link;

    public HeadlinesList(String id, String imageurl, String title, String timeTillDate, String newsTag, String link) {
        this.id = id;
        this.imageurl = imageurl;
        this.title = title;
        this.timeTillDate = timeTillDate;
        this.newsTag = newsTag;
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public String getImageurl() {
        return imageurl;
    }

    public String getTitle() {
        return title;
    }

    public String getTimeTillDate() {
        return timeTillDate;
    }

    public String getNewsTag() {
        return newsTag;
    }

    public String getLink() {
        return link;
    }
}
