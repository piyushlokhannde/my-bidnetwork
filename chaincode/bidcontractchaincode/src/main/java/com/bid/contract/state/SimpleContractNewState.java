package com.bid.contract.state;

import com.bid.contract.*;
import com.bid.contract.exception.NextStateException;


public class SimpleContractNewState extends SimpleContractState {


    @Override
    public void nextState(ContractFunctionData contract) throws NextStateException {
       if(!isValidPeriod(contract))
           throw new NextStateException("Contract Can not be moved to bidding state");
       contract.setState(ContractState.OPEN_FOR_BID);
    }


    private boolean isValidPeriod(ContractFunctionData contract) {
        return (CurrentDateProvider.isCurrentDateBetweenDate(contract.getBidStartDate(), contract.getBidEndDate()));
    }


    @Override
    public boolean isBidDataAllowed() {
        return false;
    }




}
