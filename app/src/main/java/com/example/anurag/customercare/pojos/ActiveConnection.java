package com.example.anurag.customercare.pojos;

/**
 * Created by anurag on 20/05/17.
 */

public class ActiveConnection {

    private String  customerId;

    private String  executiveId;

    private boolean activeStatus;

    public ActiveConnection() {
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getExecutiveId() {
        return executiveId;
    }

    public void setExecutiveId(String executiveId) {
        this.executiveId = executiveId;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }
}
