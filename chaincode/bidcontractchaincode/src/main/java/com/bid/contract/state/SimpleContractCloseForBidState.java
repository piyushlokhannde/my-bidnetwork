package com.bid.contract.state;

import com.bid.contract.Bid;
import com.bid.contract.ContractFunctionData;
import com.bid.contract.ContractState;
import com.bid.contract.exception.NextStateException;


import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SimpleContractCloseForBidState extends SimpleContractState {
    @Override
    public void nextState(ContractFunctionData contract) throws NextStateException {
        Bid winner = findBidWinner(contract.getBids());
        if(Objects.nonNull(winner.getContractor()) && Objects.nonNull(winner.getContractQuote())) {
            contract.setState(ContractState.ASSIGNED);
        } else  {
            contract.setState(ContractState.BID_FAIL);
        }
        contract.allocateContract(winner);
    }


    private Bid findBidWinner(List<Bid> bidList)  {
        return  bidList.stream().max(Comparator.comparing(Bid::getContractQuote))
                .orElse(new Bid(null, null));
    }



}
