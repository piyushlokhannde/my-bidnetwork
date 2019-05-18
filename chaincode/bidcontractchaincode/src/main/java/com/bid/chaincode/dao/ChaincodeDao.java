package com.bid.chaincode.dao;


import com.bid.chaincode.ContractChainCode;
import com.bid.contract.*;
import com.bid.contract.exception.BidRejectException;
import com.bid.service.dao.ContractDAO;
import com.bid.service.dto.ContractDto;
import com.bid.service.dto.ContractDtoAssembler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.util.internal.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

public class ChaincodeDao  implements ContractDAO {
    private static Log log = LogFactory.getLog(ContractChainCode.class);

    public static final String BID_COLLECTION_NAME = "bid_collection";
    public static final String DD_MM_YYYY = "dd/MM/yyyy";

    private ChaincodeStub stub;
    private Map<String, String> collectionNames = new HashMap<>();

    public ChaincodeDao(ChaincodeStub stub) {
        this.stub = stub;
        collectionNames.put("contractor1", "contractor1_bid_collection");
        collectionNames.put("contractor2", "contractor2_bid_collection");
    }


    @Override
    public void addContract(Contract contract) {
        addOrUpdateContractToLedger(contract);
    }

    @Override
    public Contract getContract(String contractID, Bid bid) {
        String contractString = stub.getStringState(contractID);
        return createContractFromLedger(contractString, contractID, bid);
    }

    @Override
    public Boolean isContractExists(String contractID) {
        String contractString = stub.getStringState(contractID);
        if (StringUtil.isNullOrEmpty(contractString)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public int getBids(String contractId) {
        return this.getBidderList(contractId, null).size();
    }

    @Override
    public void addBid(Contract contract, Bid bid) {
        BidData bidData = new BidData(bid, contract.getContractId().getId());
        stub.putPrivateData(getCollectionName(bid), contract.getContractId().getId(), convertObjectToJson(bidData));
        log.info("added the data into private database current Bid count is "+ contract.getBidCount());
        incrementBidCountAndUpdateLedger(contract);

    }


    @Override
    public void isValidContractor(String contractorId) throws BidRejectException {
      //todo to implment if contractor is valid or not
    }

    @Override
    public String getContractStatus(String contractId) {
        return getContract(contractId, null).getState().toString();
    }

    @Override
    public void updateContractNewStatus(Contract contract, ContractState state) {
       this.addOrUpdateContractToLedger(contract);
    }

    @Override
    public ContractDto getContractDto(String contractID) {
        String contractString = stub.getStringState(contractID);
        Gson gson = new Gson();
        ContractDto dto = gson.fromJson(contractString, ContractDto.class);
        return dto;
    }


    private <T extends Object> String convertObjectToJson(T argument) {

        Gson gson =  new GsonBuilder().setDateFormat(DD_MM_YYYY).create();
        String jsonStr = gson.toJson(argument);
        return jsonStr;
    }

    private String getCollectionName(Bid bid) {
        String collectionName = this.collectionNames.get(bid.getContractor().getId());
        if(StringUtil.isNullOrEmpty(collectionName)) {
            log.error("Collection name is missig for the contractor:" +bid.getContractor().getId());
            throw new RuntimeException("Collection name is missig for the contractor:" +bid.getContractor().getId());
        }
        return collectionName;
    }


    private void incrementBidCountAndUpdateLedger(Contract contract) {
        ContractDto dto =  getContractDto(contract.getContractId().getId());
        dto.setBidCount(dto.getBidCount()+1);
        uppdateLedger(dto);
    }

    private void addOrUpdateContractToLedger(Contract contract) {
        ContractDto dto = convertToContractDto(contract);
        uppdateLedger(dto);
    }

    private void uppdateLedger(ContractDto dto) {
        log.error("Putting data to BlockChain "+ convertObjectToJson(dto));
        stub.putStringState(dto.getContractID(), convertObjectToJson(dto));
        log.error("Putting data to Blockchain success");
    }

    private ContractDto convertToContractDto(Contract contract) {
        return ContractDtoAssembler.assemble(contract);
    }

    private Contract createContractFromLedger(String contractString, String contractID,  Bid bid) {
        Gson gson = new Gson();
        ContractDto dto = gson.fromJson(contractString, ContractDto.class);
        SimpleContract.ContractBuilder  builder = SimpleContract
                .getContractBuilder(ContractState.getContractState(dto.getState()))
                .setId(dto.getContractID())
                .setBidStartDate(dto.getBidStartDate())
                .setBidEndDate(dto.getBidEndDate())
                .setDescription(dto.getDescription())
                .setMinimumBid(dto.getMinimumBid());

        String contractor =  dto.getContractor();
        String assignedDate = dto.getAssignedDate();

        if(!StringUtil.isNullOrEmpty(assignedDate)) {
            AllotementData  allotementData = AllotementData
                    .getAllotementData(new Contractor(contractor), LocalDate.parse(assignedDate));
            builder.setAllotement(allotementData);
        }
        builder.setBidders(adjustBidderList(contractID, bid, dto));
        return builder.build();
    }

    private List<Bid>  adjustBidderList(String contractID, Bid bid, ContractDto dto) {
        List<Bid> bidList =  getBidderList(contractID, bid);
        while(dto.getBidCount() > bidList.size()) {
            bidList.add(new Bid(new Contractor("Fake"),BigDecimal.ZERO ));
        }
        return bidList;
    }


    private List<Bid> getBidderList(String contractID, Bid bid) {
        List<Bid> bidList = new ArrayList<>();
        Stream<String> collections = null;

        if(!Objects.isNull(bid))  {
            collections =  this.collectionNames.values().stream().filter(collectionName -> collectionName
                    .contains(bid.getContractor().getId()));
        } else {
            collections =  this.collectionNames.values().stream();
        }
        collections.forEach(collection -> addBidToList(bidList, collection, contractID));
        log.info("Number of bids present for the contract "+contractID+" BidList: " +bidList.size());
        return bidList;
    }


    private void addBidToList(List<Bid> bidList , String collectionName, String contractID) {
       Bid bid =  getPrivateDataForContractor(contractID, collectionName);
       if(bid != null) {
           bidList.add(bid);
       }
    }

    private Bid getPrivateDataForContractor(String contractID, String collectionName) {
        Bid bid = null;
        try {
            String data  = stub.getPrivateDataUTF8(collectionName, contractID);
            log.info("Fetch the private data: "+contractID+" "+  collectionName+"   "+data);
            if (!StringUtil.isNullOrEmpty(data)) {
                bid = parseBidData(data);
            }
        } catch (RuntimeException e) {
            log.error("Fetch the private data: "+contractID+ "   "+collectionName);
            log.error("Error to fetch the private data", e);
        }
        return bid;
    }

    private Bid parseBidData(String bidDataString) {
        if(!StringUtil.isNullOrEmpty(bidDataString)) {
            Gson gson = new Gson();
            BidData bidData = gson.fromJson(bidDataString, BidData.class);
            return bidData.getBid();
        } else {
            return null;
        }

    }

}

class BidData {
 private String contractorID;
 private String contractID;
 private String quote;
 private String biddingDate;

 public BidData (String contractorID, String contractID, String quote) {
     this.contractorID = contractorID;
     this.contractID = contractID;
     this.quote = quote;
     this.biddingDate = LocalDate.now().toString();
 }

 public BidData(Bid bid, String contractID) {
     this.contractID = contractID;
     this.contractorID = bid.getContractor().getId();
     this.quote = bid.getContractQuote().toString();
     this.biddingDate = LocalDate.now().toString();
 }

  public Bid getBid() {
     Contractor contractor = new Contractor(this.contractorID);
     Bid bid = new Bid(contractor, BigDecimal.valueOf(Float.parseFloat(quote)));
     return  bid;
    }
}
