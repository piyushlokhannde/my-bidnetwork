package com.bid.contract;


import com.bid.contract.exception.BidRejectException;
import com.bid.contract.exception.NextStateException;
import com.bid.contract.state.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SimpleContract extends AbstractContract  implements  ContractFunctionData {



    static  {
        stateMap.put(ContractState.NEW, new SimpleContractNewState());
        stateMap.put(ContractState.OPEN_FOR_BID, new SimpleContractOpenForBidState());
        stateMap.put(ContractState.CLOSE_FOR_BID, new SimpleContractCloseForBidState());
        stateMap.put(ContractState.ASSIGNED, new SimpleContractAssignedState());
        stateMap.put(ContractState.BID_FAIL, new SimpleContractDefaultState());
    }

    public  ContractState nextState() throws NextStateException {
        contractFunction.nextState(this);
        return this.state;
    }

    public void acceptBid(Bid bid) throws BidRejectException {
        this.contractFunction.acceptBid(bid, this);
    }

    @Override
    public List<Bid> getBids() {
        if(!contractFunction.isBidDataAllowed())
            throw new RuntimeException("Bid data not allowed in this state");
        return  bidList;
    }


    private void setBids(List<Bid> bidList) {
        /*if(!contractFunction.isBidDataAllowed())
            throw new RuntimeException("Bid data not allowed in this state");*/
        if(contractFunction.isBidDataAllowed() && Objects.nonNull(bidList))
          this.bidList = bidList;
    }

    private void setAllotment(AllotementData allotment) {
        if(checkIfAllotementAllowed() && Objects.nonNull(allotment)) {
            this.assignedDate = allotment.getAssignedDate();
            this.contractor = allotment.getContractor();
        }
    }

    private boolean checkIfAllotementAllowed() {
        return this.contractFunction.isAllotmentAllowed();
    }

    @Override
    public int getBidCount() {
        return super.getBidCount();
    }

    public static ContractBuilder getContractBuilder(ContractState state) {
        return new ContractBuilder(state);
    }


    @Override
    public void setState(ContractState contractState) {
        this.state = contractState;
        this.contractFunction = this.stateMap.get(contractState);
    }

    @Override
    public void addBidToList(Bid bid) throws BidRejectException {
        if(isBidPresent(bid)) {
            throw new BidRejectException("bid is already present");
        }
        bidList.add(bid);
    }


    @Override
    public void allocateContract(Bid bid) {
        this.assignedDate = CurrentDateProvider.getDate();
        this.contractor = bid.getContractor();
    }

    @Override
    public Contractor getAllottedContractor() {
        throwExcptionIfAllotmentNotAllowed();
        return  this.contractor;
    }

    private void throwExcptionIfAllotmentNotAllowed() {
        if(!checkIfAllotementAllowed()) {
            throw new RuntimeException("Allotment Data not present at this state");
        }
    }

    @Override
    public LocalDate getAllottedDate() {
        throwExcptionIfAllotmentNotAllowed();
       return this.assignedDate;
    }

    private boolean isBidPresent(Bid bid) {
        return bidList.stream().filter(e ->e.getContractor().getId().equals(bid.getContractor().getId())).count() > 0;
    }




    public static class  ContractBuilder  {

        private String id;
        private LocalDate bidStartDate;
        private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
        private LocalDate bidEndDate;
        private String description;
        private String  minimumBid;
        private ContractState state;
        private List<Bid> bidList;
        private AllotementData allotementData;


        public ContractBuilder(ContractState state) {
            this.state = state;
        }


        public ContractBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public ContractBuilder setBidStartDate(String date) {
            bidStartDate = LocalDate.parse(date, formatter);
            return this;
        }

        public Contract build() {
            SimpleContract contract = new SimpleContract();
            contract.contractID = new ID(id);
            contract.bidStartDate = bidStartDate;
            contract.bidEndDate = bidEndDate;
            contract.description = this.description;
            contract.minimumBid = new BigDecimal(minimumBid);
            contract.state = state;
            contract.contractFunction = contract.stateMap.get(state);

            if(Objects.nonNull(bidList))
                contract.setBids(bidList);

            if(Objects.nonNull(allotementData))
              contract.setAllotment(allotementData);
            return contract;
        }

        public ContractBuilder setBidEndDate(String date) {
            bidEndDate = LocalDate.parse(date, formatter);
            return this;
        }

        public ContractBuilder setDescription(String description) {
            this.description = description;
            return  this;
        }

        public ContractBuilder setMinimumBid(String minimumBid) {
            this.minimumBid = minimumBid;
            return this;
        }


        public ContractBuilder setBidders(List<Bid> bidList) {
            this.bidList = bidList;
            return this;
        }

        public ContractBuilder setAllotement(AllotementData allotementData) {
            this.allotementData = allotementData;
            return this;
        }
    }
}
