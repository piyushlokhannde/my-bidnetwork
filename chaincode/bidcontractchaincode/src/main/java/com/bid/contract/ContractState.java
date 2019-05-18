package com.bid.contract;

import java.util.Arrays;

public enum ContractState {

    NEW, OPEN_FOR_BID, CLOSE_FOR_BID,ASSIGNED, BID_FAIL;

    public static ContractState getContractState(String state) {
       return  Arrays.stream(ContractState.values())
                .filter(states -> states.toString().equals(state))
               .findFirst().orElse(null);

    }

}
