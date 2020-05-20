package com.example.newsplug.ui.detailspage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class detailspagePOJO {
    private String id;
    private String imageurl;
    private String title;
    private String date;
    private String desc;
    private String link;
    private String tag;

    public detailspagePOJO(String id, String imageurl, String title, String date, String desc,
                           String link, String tag) {
        this.id = id;
        this.imageurl = imageurl;
        this.title = title;
        this.date = date;
        this.desc = desc;
        this.link = link;
        this.tag = tag;
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

    public String getDate() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("PST"));
        String[] dt = format.parse(date).toString().split(" ");
        String FinalDate = dt[2] + " " + dt[1] + " " + dt[5];
        return FinalDate;
    }

    public String getDesc() {
        return desc;
    }

    public String getLink() {
        return link;
    }

    public String getTag() {
        return tag;
    }
}
