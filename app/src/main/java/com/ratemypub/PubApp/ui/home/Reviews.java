package com.ratemypub.PubApp.ui.home;

public class Reviews {

    public String description;
    public String pubName;
    public Double rating;


    public Reviews() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Reviews(String description, String pubName, Double rating) {
        this.description = description;
        this.pubName = pubName;
        this.rating = rating;
    }
}
