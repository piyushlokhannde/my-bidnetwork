package com.bid.contract.state;


import com.bid.contract.Bid;
import com.bid.contract.ContractFunctionData;
import com.bid.contract.exception.BidRejectException;
import com.bid.contract.exception.NextStateException;


public interface ContractFunction {

    void acceptBid(Bid bid, ContractFunctionData contract) throws BidRejectException;

    void nextState(ContractFunctionData contract) throws NextStateException;


    boolean isBidDataAllowed();

    boolean isAllotmentAllowed();




}