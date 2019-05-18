package com.bid.contract.state;

import com.bid.contract.*;
import com.bid.contract.exception.BidRejectException;

import com.bid.contract.exception.NextStateException;

public class SimpleContractOpenForBidState extends SimpleContractState {



    @Override
    public void acceptBid(Bid bid, ContractFunctionData contract) throws BidRejectException {
        if(isValidQuote(bid, contract))
            throw new BidRejectException("Quote is less than the minimum quote");
        contract.addBidToList(bid);
    }

    @Override
    public void nextState(ContractFunctionData contract) throws NextStateException {
        isBiddingPeridOver(contract);
        contract.setState(ContractState.CLOSE_FOR_BID);
    }

    private boolean isValidQuote(Bid bid, Contract contract) {
        return bid.getContractQuote().compareTo(contract.getMinimumBid())  == -1;
    }


    private void isBiddingPeridOver(Contract contract) throws NextStateException {
        if(CurrentDateProvider.isDateBeforeCurrentDate(contract.getBidEndDate()))
            throw new NextStateException("Can not move to the CLOSE_FOR_BID state");
    }
}
