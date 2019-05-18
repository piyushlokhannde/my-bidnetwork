package com.bid.service.dto;

import com.bid.contract.Contract;
import com.bid.contract.ContractState;

import java.time.format.DateTimeFormatter;


public class ContractDtoAssembler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static ContractDto assemble(Contract contract) {
        ContractDto contractDto = new ContractDto();
        contractDto.setBidCount(contract.getBidCount());
        contractDto.setBidEndDate(contract.getBidEndDate().format(DATE_TIME_FORMATTER));
        contractDto.setBidStartDate(contract.getBidStartDate().format(DATE_TIME_FORMATTER));
        contractDto.setContractID(contract.getContractId().getId());
        contractDto.setDescription(contract.getDescription());
        contractDto.setMinimumBid(contract.getMinimumBid().toString());
        contractDto.setState(contract.getState().toString());

        if(ContractState.ASSIGNED.equals(contract.getState())) {
            contractDto.setAssignedDate(contract.getAllottedDate().toString());
            contractDto.setContractor(contract.getAllottedContractor().getId());
        }

        return  contractDto;
    }
}
