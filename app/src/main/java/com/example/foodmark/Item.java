package com.example.foodmark;

public class Item {

    String name;
    String location;
    String description;
    int image;

    public Item() {
        this("", "", "", 0);
    }

    public Item(String name, String location, String description, int image) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

}