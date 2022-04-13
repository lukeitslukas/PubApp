package com.ratemypub.PubApp.ui.map;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Coordinates {

    public Double lat;
    public Double lng;
    public String placeName;
    public Double rating;

    public Coordinates() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Coordinates(Double lat, Double lng, String placeName, Double rating) {
        this.lat = lat;
        this.lng = lng;
        this.placeName = placeName;
        this.rating = rating;
    }

}
