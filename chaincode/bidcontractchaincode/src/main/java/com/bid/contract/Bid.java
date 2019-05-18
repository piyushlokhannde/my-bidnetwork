package com.bid.contract;

import java.math.BigDecimal;

public class Bid {
    private Contractor contractor;
    private BigDecimal contractQuote;

    public Bid(Contractor contractor, BigDecimal contractQuote) {
        this.contractor = contractor;
        this.contractQuote = contractQuote;
    }

    public Contractor getContractor() {
        return contractor;
    }

    public BigDecimal getContractQuote() {
        return contractQuote;
    }
}
