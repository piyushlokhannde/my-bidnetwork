package com.bid.contract;

import com.bid.contract.exception.BidRejectException;
import com.bid.contract.exception.NextStateException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;

public interface Contract  {

    ContractState nextState() throws NextStateException;

    ID getContractId();

    ContractState getState();

    Contractor getAllottedContractor();

    LocalDate getAllottedDate();

    ChronoLocalDate getBidEndDate();

    ChronoLocalDate getBidStartDate();

    BigDecimal getMinimumBid();

    void acceptBid(Bid bid) throws BidRejectException;

    int getBidCount();

    String getDescription();
}
