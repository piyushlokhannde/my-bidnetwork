package com.bid.chaincode;

import com.bid.chaincode.dao.ChaincodeDao;
import com.bid.contract.ContractState;
import com.bid.contract.CurrentDateProvider;
import com.bid.contract.exception.NextStateException;
import com.bid.service.BidService;
import com.bid.service.ContractNotExists;
import com.bid.service.dto.ContractDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;

public class AllocateContractChaincode extends ChaincodeBase {

    private static Log log = LogFactory.getLog(AllocateContractChaincode.class);

    @Override
    public Response init(ChaincodeStub chaincodeStub) {
        return newSuccessResponse();
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        CurrentDateProvider.enableMockMode();
        String function = stub.getFunction();
        Response response;
        switch (function) {

            case "assingcontract":
                response = assignContract(stub);
                break;
            default:
                response = newErrorResponse("No correct function name selected");
        }
        return response;
    }

    private Response assignContract(ChaincodeStub stub) {
        BidService service = createBidService(stub);

        Response validationResponse = checkContractIdInInput(stub);
        if(validationResponse != null) {
            return  validationResponse;
        }

        List<String> param = stub.getParameters();
        try {
            log.info("Entering the method assignContract ");
            validateIfContractIsInCloseForBid(param.get(0), service);
            ContractState state =  service.assignedContract(param.get(0));
            log.info("leaving the method assignContract ");
            return newSuccessResponse("Contract: " + param.get(0)+ " is moved to the state :" +state.toString());
        } catch (NextStateException e) {
            log.error(" movenextstate-allocatecc:Failed to move the contract to next stage with id  "+ param.get(0), e);
            return newErrorResponse("movenextstate-allocatecc:Failed to move the contract to next stage with id  "+ param.get(0));
        } catch (Exception e) {
            log.error(" movenextstate-allocatecc:Failed to move the contract to next stage with id  "+ param.get(0), e);
            return newErrorResponse("movenextstate-allocatecc :Failed to move the contract to next stage with id  "+ param.get(0));
        }
    }


    private void validateIfContractIsInCloseForBid(String contractID, BidService bidService) throws ContractNotExists {
        ContractDto contractDto = this.getContractFromLedger(contractID, bidService);
        if(!contractDto.getState().equals(ContractState.CLOSE_FOR_BID.toString())) {
            throw new RuntimeException("Can not move to the next state for contract with status " +
                    contractDto.getState());
        }
    }

    private ContractDto getContractFromLedger(String contractId, BidService service) throws ContractNotExists {
        ContractDto contractDto =  service.getContract(contractId);
        return contractDto;
    }



    private Response checkContractIdInInput(ChaincodeStub stub) {
        List<String> param = stub.getParameters();

        if(param.size() != 1) {
            log.error(" getContract : Contract Id is missing");
            return newErrorResponse("getContract : Contract Id is missing");
        }

        return  null;

    }


    private BidService createBidService(ChaincodeStub stub) {
        ChaincodeDao dao = new ChaincodeDao(stub);
        BidService bidService = new BidService();
        bidService.setContractDAO(dao);
        return bidService;
    }

      public static void main(String[] args) {
        System.out.println("Application started");
        new AllocateContractChaincode().start(args);
    }
}
