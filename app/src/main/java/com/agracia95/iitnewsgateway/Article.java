package com.agracia95.iitnewsgateway;

import java.io.Serializable;
import java.util.Date;

public class Article implements Serializable
{
    private String author;
    private String title;
    private String description;
    private String url;
    private Date date;
    private String imageUrl;

    public Article(String author, String title, String description, String url, String imageUrl, Date date)
    {
        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }

    public Date getDate() { return date; }

    public void setDate(Date date) { this.date = date; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

}
