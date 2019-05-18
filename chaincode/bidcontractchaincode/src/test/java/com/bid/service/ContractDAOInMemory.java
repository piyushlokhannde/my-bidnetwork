package com.bid.service;

import com.bid.contract.Bid;
import com.bid.contract.Contract;
import com.bid.contract.ContractState;
import com.bid.contract.Contractor;
import com.bid.contract.exception.BidRejectException;
import com.bid.service.dao.ContractDAO;
import com.bid.service.dto.ContractDto;
import com.bid.service.dto.ContractDtoAssembler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractDAOInMemory implements ContractDAO {

    Map<String,Contract> contractMap = new HashMap<>();

    Map<String, List<Bid>> bidMap = new HashMap<>();

    List<String> contractorList = new ArrayList<>();

    Map<String,ContractStatus> contractStatusMap = new HashMap<>();

    public ContractDAOInMemory () {

        contractorList.add("contractor1");
        contractorList.add("contractor2");
    }


    @Override
    public void addContract(Contract contract) {
        contractMap.put(contract.getContractId().getId(), contract);
    }

    @Override
    public Contract getContract(String contractID, Bid bid) {
        return contractMap.get(contractID);
    }

    @Override
    public Boolean isContractExists(String contractID) {
        return contractMap.containsKey(contractID);
    }

    @Override
    public int getBids(String contractId) {
        return bidMap.get(contractId).size();
    }

    @Override
    public void addBid(Contract contract, Bid bid) {
        if(!bidMap.containsKey(contract.getContractId().getId())) {
            bidMap.put(contract.getContractId().getId(), new ArrayList<Bid>());
        }
        bidMap.get(contract.getContractId().getId()).add(bid);
    }

    @Override
    public void isValidContractor(String contractorId) throws BidRejectException {
        if(!contractorList.contains(contractorId)) {
            throw new BidRejectException("Invalid Contractor");
        }

    }

    public String getContractStatus(String contractId) {
        return contractStatusMap.get(contractId).state.toString();
    }

    public Contractor getAllotedContractor(String contractId) {
        return contractStatusMap.get(contractId).allottedContractor;
    }
    public LocalDate getAlloteddate(String contractId) {
        return contractStatusMap.get(contractId).allottedDate;
    }


    @Override
    public void updateContractNewStatus(Contract contract, ContractState state) {
        if(!contractStatusMap.containsKey(contract.getContractId().getId())) {
            contractStatusMap.put(contract.getContractId().getId(),new ContractStatus());
        }

        ContractStatus status = contractStatusMap.get(contract.getContractId().getId());
        status.state = contract.getState();
        if(ContractState.ASSIGNED.equals(status.state)) {
            status.allottedContractor = contract.getAllottedContractor();
            status.allottedDate = contract.getAllottedDate();
        }

    }

    @Override
    public ContractDto getContractDto(String contractID) {
        Contract contract = contractMap.get(contractID);
        return  ContractDtoAssembler.assemble(contract);
    }
/*
    @Override
    public void updateStatusAllottedContract(Contract contract) {
        if(!contractStatusMap.containsKey(contract.getContractId().getId())) {
           contractStatusMap.put(contract.getContractId().getId(),new ContractStatus());
        }
        ContractStatus status = contractStatusMap.get(contract.getContractId().getId());
        status.state = contract.getState();
        status.allottedContractor = contract.getAllottedContractor();
        status.allottedDate = contract.getAllottedDate();

    }*/


    class ContractStatus {
        ContractState state;
        Contractor allottedContractor;
        LocalDate allottedDate;
    }
}
