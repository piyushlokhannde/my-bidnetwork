package com.bid.service;

import com.bid.contract.*;
import com.bid.contract.exception.BidRejectException;
import com.bid.contract.exception.NextStateException;
import com.bid.service.dao.ContractDAO;
import com.bid.service.dto.ContractDto;
import com.bid.service.dto.ContractDtoAssembler;

import java.math.BigDecimal;

public class BidService {

    private ContractDAO contractDAO;



    public ContractDto createContract(String id, String startDate, String endDate, String description, String quote) throws
            DuplicateContractException  {
        isContractExists(id);
        Contract contract = createContractObject(id, startDate, endDate, description, quote);
        contractDAO.addContract(contract);
        return ContractDtoAssembler.assemble(contract);
    }

    public void bidForContract(String contractId, String contractorId, String quote) throws BidRejectException {
       isValidContractor(contractorId);
        Bid bid = createBid(contractorId, quote);
       Contract contract = contractDAO.getContract(contractId, bid);
       contract.acceptBid(bid);
       contractDAO.addBid(contract, bid);
    }

    public void setContractDAO(ContractDAO contractDAO) {
        this.contractDAO = contractDAO;
    }

    public ContractState changeNextStatus(String contractId) throws NextStateException {
        Bid bid = createBid("INVALID", "0");
        Contract contract = contractDAO.getContract(contractId, bid);
        ContractState state = contract.nextState();
        contractDAO.updateContractNewStatus(contract, state);
        return state;
    }


    public ContractState assignedContract(String contractId) throws NextStateException {
        Contract contract = contractDAO.getContract(contractId, null);
        ContractState state = contract.nextState();
        contractDAO.updateContractNewStatus(contract, state);
        return state;
    }

    public ContractDto getContract(String contractID) throws ContractNotExists {

        if(!contractDAO.isContractExists(contractID)) {
            throw new ContractNotExists();
        }

        return contractDAO.getContractDto(contractID);
    }

    private void isValidContractor(String contractorId) throws BidRejectException {
        contractDAO.isValidContractor(contractorId);
    }

    private Bid createBid(String contractorId, String quote) {
        Contractor contractor = new Contractor(contractorId);
        Bid bid  = new Bid(contractor, new BigDecimal(quote));
        return bid;
    }

    private Contract createContractObject(String id, String startDate, String endDate,
                                          String description, String quote) {
        return SimpleContract.getContractBuilder(ContractState.NEW).setId(id)
                .setBidStartDate(startDate)
                .setBidEndDate(endDate)
                .setDescription(description)
                .setMinimumBid(quote)
                .build();
    }

    private void isContractExists(String contractId) throws DuplicateContractException {
        if(contractDAO.isContractExists(contractId))
            throw new DuplicateContractException();
    }
}
