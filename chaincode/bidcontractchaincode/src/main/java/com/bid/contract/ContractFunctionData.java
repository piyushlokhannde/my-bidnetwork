package com.bid.contract;


import com.bid.contract.exception.BidRejectException;


import java.util.List;

public interface ContractFunctionData  extends  Contract  {


    void setState(ContractState contractState);

    void addBidToList(Bid bid) throws BidRejectException;

    void allocateContract(Bid bid);

    List<Bid> getBids();


}
