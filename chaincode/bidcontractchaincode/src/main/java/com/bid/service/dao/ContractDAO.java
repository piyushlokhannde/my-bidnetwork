package com.bid.service.dao;

import com.bid.contract.Bid;
import com.bid.contract.Contract;
import com.bid.contract.ContractState;
import com.bid.contract.exception.BidRejectException;
import com.bid.service.dto.ContractDto;

public interface ContractDAO {

    void addContract(Contract contract);

    Contract getContract(String contractID, Bid bid);

    Boolean isContractExists(String contractID);

    int getBids(String contractId);

    void addBid(Contract contract, Bid bid);

    void isValidContractor(String contractorId) throws BidRejectException;

    String getContractStatus(String contractId);

    void updateContractNewStatus(Contract contract, ContractState state);

    ContractDto getContractDto(String contractID);

}
