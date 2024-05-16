package com.example.foodmark;

import com.mapbox.geojson.GeoJson;

public class Item {

    String locationImage;
    String title;
    String location;
    String latitude;
    String longitude;
    String description;
    String category;
    String id;

    public Item() {}

    public Item(String locationImage, String title, String location, String latitude, String longitude, String description, String category, String id) {
        this.locationImage = locationImage;
        this.title = title;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.category = category;
        this.id = id;
    }


    public String getLocationImage() {
        return locationImage;
    }

    public void setLocationImage(String locationImage) {
        this.locationImage = locationImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}