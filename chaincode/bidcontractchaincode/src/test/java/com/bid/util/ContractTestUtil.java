package com.bid.util;

import com.bid.contract.Bid;
import com.bid.contract.ContractState;
import com.bid.contract.Contractor;
import com.bid.contract.SimpleContract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ContractTestUtil {

    public static final String contractID = "PWD-12-9023";
    public static final String BID_START_DATE = "01/10/2019";
    public static final String BID_END_DATE = "01/11/2019";
    public static final String DESCRIPTION = "PWD contract";
    public static final String MINIMUM_BID = "500";


    public static SimpleContract.ContractBuilder getContractBuilderWithState(ContractState contractState) {
        return SimpleContract.getContractBuilder(contractState)
                .setId(contractID)
                .setBidStartDate(BID_START_DATE)
                .setBidEndDate(BID_END_DATE)
                .setDescription(DESCRIPTION)
                .setMinimumBid(MINIMUM_BID);
    }


    public static Bid getBid(String contractorId, float quoteValue) {
        Contractor contractor = new Contractor(contractorId);
        BigDecimal quote = new BigDecimal(quoteValue);
        return new Bid(contractor, quote);
    }

    public static List<Bid> getBidList() {
        List<Bid> bidList = new ArrayList<>();
        bidList.add(getBid("contractor", 200));
        return  bidList;
    }

}
