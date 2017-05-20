package com.example.anurag.customercare.pojos;

import java.util.List;

/**
 * Created by anurag on 20/05/17.
 */

public class Executive {

    private String       executiveId;

    private double         rating;

    private List<String> tags;

    public String getExecutiveId() {
        return executiveId;
    }

    public void setExecutiveId(String executiveId) {
        this.executiveId = executiveId;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
