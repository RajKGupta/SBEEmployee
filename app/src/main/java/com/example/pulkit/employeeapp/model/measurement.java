package com.example.pulkit.employeeapp.model;

public class measurement {
    private String tag;
    private String width;
    private String height;
    private String fleximage;
    private String unit;
    private String id;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public measurement(String tag, String width, String height, String fleximage, String unit,String id) {
        this.tag = tag;
        this.width = width;
        this.height = height;
        this.fleximage = fleximage;
        this.unit = unit;
        this.id = id;
    }

    public measurement() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getFleximage() {
        return fleximage;
    }

    public void setFleximage(String fleximage) {
        this.fleximage = fleximage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
