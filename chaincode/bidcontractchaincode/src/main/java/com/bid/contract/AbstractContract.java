package com.bid.contract;


import com.bid.contract.state.ContractFunction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public abstract class  AbstractContract  implements Contract {
    protected ID contractID;
    protected LocalDate bidStartDate;
    protected LocalDate bidEndDate;
    protected String description;
    protected ContractState state;
    protected BigDecimal minimumBid;
    protected List<Bid> bidList = new ArrayList<>();
    protected ContractFunction contractFunction;
    protected Contractor contractor;
    protected LocalDate assignedDate;
    protected static Map<ContractState, ContractFunction> stateMap = new HashMap<>();




    public ID getContractId() {
        return contractID;
    }

    public LocalDate getBidStartDate() {
        return this.bidStartDate;
    }

    public LocalDate getBidEndDate() {
        return this.bidEndDate;
    }

    public String getDescription() {
        return  this.description;
    }

    public ContractState getState() {
        return state;
    }

    public int getBidCount() {
        return Objects.isNull(bidList)? 0:bidList.size();
    }

    public BigDecimal getMinimumBid() {
        return minimumBid;
    }



}
