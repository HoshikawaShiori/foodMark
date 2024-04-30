package com.example.foodmark;

public class Item {

    String name;
    String Location;
    String Description;
    int image;

    public Item() {
    }

    public Item(String name, String location, String description, int image) {
        this.name = name;
        Location = location;
        Description = description;
        this.image = image;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }


}
