package com.bid.service.dto;



public class ContractDto {


    private String contractID;
    private String bidStartDate;
    private String bidEndDate;
    private String description;
    private String state;
    private String minimumBid;
    private int bidCount;
    private String contractor;
    private String assignedDate;


    public String getContractID() {
        return contractID;
    }

    public void setContractID(String contractID) {
        this.contractID = contractID;
    }

    public String getBidStartDate() {
        return bidStartDate;
    }

    public void setBidStartDate(String bidStartDate) {
        this.bidStartDate = bidStartDate;
    }

    public String getBidEndDate() {
        return bidEndDate;
    }

    public void setBidEndDate(String bidEndDate) {
        this.bidEndDate = bidEndDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMinimumBid() {
        return minimumBid;
    }

    public void setMinimumBid(String minimumBid) {
        this.minimumBid = minimumBid;
    }

    public int getBidCount() {
        return bidCount;
    }

    public void setBidCount(int bidCount) {
        this.bidCount = bidCount;
    }

    public String getContractor() {
        return contractor;
    }

    public void setContractor(String contractor) {
        this.contractor = contractor;
    }

    public String getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(String assignedDate) {
        this.assignedDate = assignedDate;
    }



}
