package com.bid.contract.state;


import com.bid.contract.*;
import com.bid.contract.exception.BidRejectException;
import com.bid.contract.exception.NextStateException;


public abstract  class SimpleContractState implements ContractFunction {

    @Override
    public void nextState(ContractFunctionData contract) throws NextStateException {
        throw new NextStateException("Termination state of the contract");
    }

    @Override
    public void acceptBid(Bid bid, ContractFunctionData contract) throws BidRejectException {
        throw new BidRejectException("Operation Not Allowed in this State");
    }


    @Override
    public boolean isBidDataAllowed() {
        return true;
    }

    @Override
    public boolean isAllotmentAllowed() {
        return false;
    }






}
