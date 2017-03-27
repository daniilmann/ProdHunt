package com.gglads.prodhunt.Entities;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;

import java.io.Serializable;

public class Product implements Comparable<Product>, Serializable {

    private Integer ID = null;
    private Bitmap thumb = null;
    private Bitmap screen = null;
    private String thumbLink = null;
    private String screenLink = null;
    private String name = null;
    private String desc = null;
    private String upvotes = null;

    public Product(Integer ID, String name, String desc, String upvotes) {
        this.ID = ID;
        this.name = name;
        this.desc = desc;
        this.upvotes = upvotes;
    }

    public int getID() {
        return ID;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void loadThumb(ImageView iv) {
        Ion.with(iv).load(thumbLink);
    }

    public void setThumb(String link) {
        thumbLink = link;
    }

    public void setThumb(Bitmap thumb) {
        if (thumb != null)
            this.thumb = thumb;
    }

    public Bitmap getScreen() {
        return thumb;
    }

    public String getScreenLink() {
        return screenLink;
    }

    public void setScreen(String link) {
        screenLink = link;
    }

    public void loadScreen(ImageView iv) {
        Ion.with(iv).load(screenLink);
    }

    public void setScreen(Bitmap screen) {
        if (screen != null)
            this.screen = screen;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getUpvotes() {
        return upvotes;
    }

    @Override
    public int compareTo(@NonNull Product another) {
        return ID > another.getID() ? 1 : ID < another.getID() ? -1 : 0;
    }
}
