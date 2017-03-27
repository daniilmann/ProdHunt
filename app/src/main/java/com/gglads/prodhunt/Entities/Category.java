package com.gglads.prodhunt.Entities;

public class Category {

    private Integer ID = null;
    private String name = null;
    private boolean notfication = false;

    public Category(Integer ID, String name) {
        this.ID = ID;
        this.name = name;
    }

    public Integer getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public boolean isNotfication() {
        return notfication;
    }

    public void setNotfication(boolean notfication) {
        this.notfication = notfication;
    }
}
