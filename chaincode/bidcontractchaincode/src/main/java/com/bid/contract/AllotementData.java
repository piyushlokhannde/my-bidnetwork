package com.bid.contract;

import com.bid.contract.Contractor;

import java.time.LocalDate;

public class AllotementData {

    private Contractor contractor;
    private LocalDate assignedDate;


    public static AllotementData getAllotementData(Contractor contractor, LocalDate assignedDate) {
        AllotementData allotementData = new AllotementData();
        allotementData.assignedDate  = assignedDate;
        allotementData.contractor = contractor;
        return allotementData;
    }

    public Contractor getContractor() {
        return contractor;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }


}
