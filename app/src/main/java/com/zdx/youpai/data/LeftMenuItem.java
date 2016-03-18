package com.zdx.youpai.data;

public class LeftMenuItem {

    private String name;
    private int imageId;

    public LeftMenuItem(String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public int getImageId() {
        return imageId;
    }
}
