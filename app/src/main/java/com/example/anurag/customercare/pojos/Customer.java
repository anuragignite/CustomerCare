package com.example.anurag.customercare.pojos;

import java.util.List;

/**
 * Created by anurag on 20/05/17.
 */

public class Customer {

    private String       customerId;

    private double         rating;

    private List<String> tags;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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
