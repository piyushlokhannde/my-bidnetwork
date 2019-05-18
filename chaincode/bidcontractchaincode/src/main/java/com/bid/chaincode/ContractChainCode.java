package com.bid.chaincode;

import com.bid.chaincode.dao.ChaincodeDao;
import com.bid.contract.ContractState;
import com.bid.contract.CurrentDateProvider;
import com.bid.contract.exception.BidRejectException;
import com.bid.contract.exception.NextStateException;
import com.bid.service.BidService;
import com.bid.service.ContractNotExists;
import com.bid.service.DuplicateContractException;
import com.bid.service.dto.ContractDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;

public class ContractChainCode extends ChaincodeBase {

    private static Log log = LogFactory.getLog(ContractChainCode.class);
    @Override
    public Response init(ChaincodeStub chaincodeStub) {
        return newSuccessResponse();
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        CurrentDateProvider.enableMockMode();
        String function = stub.getFunction();
        log.error("Functions name "+ function);
        log.error("Functions name "+ stub.getParameters());
        Response response;
        switch (function) {

            case "createcontract":
               response = createNewContract(stub);
                break;
            case "getcontract":
                response = getContract(stub);
                break;
            case "movenextstate":
                response = setContractToNextStage(stub);
                break;
            case "addbid":
                response = addBidForContract(stub);
                break;
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
            validateIfContractIsNotInCloseForBid(param.get(0), service);
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

    private Response addBidForContract(ChaincodeStub stub) {

        List<String> param = stub.getParameters();

        if(param.size() != 3) {
            log.error(" addBidforContract :Not all parameters are present");
            return newErrorResponse(" addBidforContract :Not all parameters are present");
        }

        BidService service = createBidService(stub);

        try {
            service.bidForContract(param.get(0), param.get(1), param.get(2));
            return newSuccessResponse("Bid added for the Contract with id"+param.get(0));
        } catch (BidRejectException e) {
            log.error(" addBidforContract:Failed to add bid for contract with id  "+ param.get(0));
            return newErrorResponse(e.getMessage()+" addBidforContract:Failed to add bid for contract with id  "+ param.get(0));
        }
    }


    private Response setContractToNextStage(ChaincodeStub stub) {

        Response validationResponse = checkContractIdInInput(stub);
        if(validationResponse != null) {
            return  validationResponse;
        }

        BidService service = createBidService(stub);
        List<String> param = stub.getParameters();
        try {
            validateIfContractIsInCloseForBid(param.get(0),service);
            ContractState state =  service.changeNextStatus(param.get(0));
            return newSuccessResponse("Contract: " + param.get(0)+ " is moved to the state :" +state.toString());
        } catch (NextStateException e) {
            log.error(" movenextstate:Failed to move the contract to next stage with id  "+ param.get(0), e);
            return newErrorResponse("movenextstate :Failed to move the contract to next stage with id  "+ param.get(0));
        } catch (Exception e) {
            log.error(" movenextstate:Failed to move the contract to next stage with id  "+ param.get(0), e);
            return newErrorResponse("movenextstate :Failed to move the contract to next stage with id  "+ param.get(0));
        }
    }

    private void validateIfContractIsInCloseForBid(String contractID, BidService bidService) throws ContractNotExists {

        ContractDto contractDto = this.getContractFromLedger(contractID, bidService);
        log.error("State is :"+contractDto.getState()+ "  "+ ContractState.CLOSE_FOR_BID.toString());
        if(contractDto.getState().equals(ContractState.CLOSE_FOR_BID.toString())) {
            throw new RuntimeException("Can not move to the next state for contract with status " +
                     ContractState.CLOSE_FOR_BID + " with bidcc chaincode, try with allocatecc");
        }
    }


    private void validateIfContractIsNotInCloseForBid(String contractID, BidService bidService) throws ContractNotExists {
        ContractDto contractDto = this.getContractFromLedger(contractID, bidService);
        if(!contractDto.getState().equals(ContractState.CLOSE_FOR_BID.toString())) {
            throw new RuntimeException("Can not move to the next state for contract with status " +
                    contractDto.getState());
        }
    }


    private Response checkContractIdInInput(ChaincodeStub stub) {
        List<String> param = stub.getParameters();

        if(param.size() != 1) {
            log.error(" getContract : Contract Id is missing");
            return newErrorResponse("getContract : Contract Id is missing");
        }

        return  null;

    }


    private Response getContract(ChaincodeStub stub) {

        List<String> param = stub.getParameters();

        Response validationResponse = checkContractIdInInput(stub);
        if(validationResponse != null) {
            return  validationResponse;
        }

        BidService service = createBidService(stub);

        try {
            ContractDto contractDto = getContractFromLedger(param.get(0), service);
            return newSuccessResponse("Contract present in the ledger", convertObjectToJson(contractDto));
        } catch (ContractNotExists e) {
            log.error("Fail to get contract with id "+param.get(0), e);
            return newErrorResponse("Fail to get contract with id "+param.get(0));
        }
    }


    private ContractDto getContractFromLedger(String contractId, BidService service) throws ContractNotExists {
        ContractDto contractDto =  service.getContract(contractId);
        return contractDto;
    }

    private Response createNewContract(ChaincodeStub stub) {

       List<String> param = stub.getParameters();

       if(param.size() != 5) {
           log.error("complete parameters are not present");
           return newErrorResponse("Not all parameters are present");
       }

        BidService service = createBidService(stub);

        try {
            ContractDto contract = service
                    .createContract(param.get(0), param.get(1), param.get(2), param.get(3), param.get(4));
           return newSuccessResponse("Created Contract Successfully", convertObjectToJson(contract));
        } catch (DuplicateContractException e) {
            log.error("Fail to create contract", e);
            return newErrorResponse("duplicate contract");
        }


    }

    private <T extends Object> byte[] convertObjectToJson(T argument) {
        Gson gson =  new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
        String jsonStr = gson.toJson(argument);
        return jsonStr.getBytes();
    }


    private BidService createBidService(ChaincodeStub stub) {
        ChaincodeDao dao = new ChaincodeDao(stub);
        BidService bidService = new BidService();
        bidService.setContractDAO(dao);
        return bidService;
    }

    public static void main(String[] args) {
        System.out.println("Application started");
        new ContractChainCode().start(args);
    }
}
