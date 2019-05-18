package com.bid.service;

import com.bid.contract.*;
import com.bid.contract.exception.BidRejectException;
import com.bid.contract.exception.NextStateException;
import com.bid.service.dto.ContractDto;
import com.bid.util.ContractTestUtil;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class BidServiceTest {

    BidService bidService;
    ContractDAOInMemory dao;


    @Before
    public void init() {
        bidService = new BidService();
        dao = new ContractDAOInMemory();
        bidService.setContractDAO(dao);
        //CurrentDateProvider.enableMockMode();

    }


    @Test
    public void given_no_duplicate_contract_is_present_new_contract_created() throws DuplicateContractException {
        ContractDto contract = createContract1();
        assertThat(contract.getContractID(),is(equalTo(ContractTestUtil.contractID)));
    }

    @Test(expected = DuplicateContractException.class)
    public void given_duplicate_contract_is_present_new_contract_is_not_created() throws DuplicateContractException {
        createContract1();
        createContract1();
    }


    @Test//for open for bid:current date should be greater then bid starting date.
    public void given_contract_NEW_state_change_to_OPEN_FOR_BID() throws NextStateException {
        createContract_in_dao_with_state(ContractState.NEW, false);
        changeCurrentDateGreaterThanBidStartDate();
        bidService.changeNextStatus(ContractTestUtil.contractID);
        assertThat(dao.getContractStatus(ContractTestUtil.contractID),
                is(equalTo(ContractState.OPEN_FOR_BID.toString())));
    }


    @Test(expected =  NextStateException.class)
    public void given_contract_NEW_state_change_to_OPEN_FOR_BID_if_current_date_below_bid_starting_date()
            throws NextStateException {
        createContract_in_dao_with_state(ContractState.NEW, false);
        changeCurrentDateEqualToBeforeThanBidStartDate();
        bidService.changeNextStatus(ContractTestUtil.contractID);
    }


    @Test(expected = NextStateException.class)
    public void given_contract_other_than_NEW_state_it_can_not_be_moved_to_OPEN_FOR_BID() throws NextStateException {
        createContract_in_dao_with_state(ContractState.ASSIGNED, false);
        changeCurrentDateGreaterThanBidStartDate();
        bidService.changeNextStatus(ContractTestUtil.contractID);
    }

    @Test
    public void given_contract_state_OPEN_FOR_BID_change_to_CLOSE_FOR_BID() throws NextStateException {
        createContract_in_dao_with_state(ContractState.OPEN_FOR_BID, false);
        changeCurrentDateAfterBidEndDate();
        bidService.changeNextStatus(ContractTestUtil.contractID);
        assertThat(dao.getContractStatus(ContractTestUtil.contractID),
                is(equalTo(ContractState.CLOSE_FOR_BID.toString())));

    }

    @Test(expected = NextStateException.class)
    public void given_contract_state_OPEN_FOR_BID_current_date_before_bid_end() throws NextStateException {
        createContract_in_dao_with_state(ContractState.OPEN_FOR_BID, false);
        changeCurrentDateEqualToBeforeThanBidEndDate();
        bidService.changeNextStatus(ContractTestUtil.contractID);
    }

    @Test
    public void given_contract_state_NOT_OPEN_FOR_BID_can_not_change_to_CLOSE_FOR_BID() throws NextStateException {
        createContract_in_dao_with_state(ContractState.NEW, false);
        changeCurrentDateEqualToBeforeThanBidEndDate();
        bidService.changeNextStatus(ContractTestUtil.contractID);
        assertThat(dao.getContractStatus(ContractTestUtil.contractID),
                not(equalTo(ContractState.CLOSE_FOR_BID.toString())));
    }

    @Test
    public void given_contract_state_CLOSE_FOR_BID_change_TO_ASSIGNED() throws NextStateException {
        set_Current_Date_for_contract_assignment_event();
        createContract_in_dao_with_state(ContractState.CLOSE_FOR_BID, true);
        bidService.changeNextStatus(ContractTestUtil.contractID);
        assertThat(dao.getContractStatus(ContractTestUtil.contractID),
                is(equalTo(ContractState.ASSIGNED.toString())));
        assertThat(dao.getAllotedContractor(ContractTestUtil.contractID),is(notNullValue()));
        assertThat(dao.getAlloteddate(ContractTestUtil.contractID),is(notNullValue()));

    }

    @Test
    public void given_contract_state_CLOSE_FOR_BID_change_TO_BID_FAIL() throws NextStateException {
        set_Current_Date_for_contract_assignment_event();
        createContract_in_dao_with_state(ContractState.CLOSE_FOR_BID, false);
        bidService.changeNextStatus(ContractTestUtil.contractID);
        assertThat(dao.getContractStatus(ContractTestUtil.contractID),
                is(equalTo(ContractState.BID_FAIL.toString())));
    }


    @Test
    public void given_contract_OPEN_FOR_BID_add_bid() throws BidRejectException {
        createContract_in_dao_with_state(ContractState.OPEN_FOR_BID, false);
        bidService.bidForContract(ContractTestUtil.contractID, "contractor1", "800");
        assertThat(dao.getBids(ContractTestUtil.contractID), is(equalTo(1)));
        bidService.bidForContract(ContractTestUtil.contractID, "contractor2", "700");
        assertThat(dao.getBids(ContractTestUtil.contractID), is(equalTo(2)));
    }

    @Test(expected = BidRejectException.class)
    public void given_invalid_contractorId_reject_bid() throws BidRejectException {
        createContract_in_dao_with_state(ContractState.OPEN_FOR_BID, false);
        bidService.bidForContract(ContractTestUtil.contractID, "invalid_contractor_id", "800");
    }

    @Test
    public void test_return_contract_with_NEW_state() throws ContractNotExists {
        createContract_in_dao_with_state(ContractState.NEW, false);
        ContractDto contract = bidService.getContract(ContractTestUtil.contractID);
        assertContractDtoFields(contract, ContractState.NEW);
    }

    @Test(expected = ContractNotExists.class)
    public void return_exception_when_contract_does_not_exit() throws ContractNotExists {
        bidService.getContract(ContractTestUtil.contractID);
    }

    @Test
    public void test_return_contract_with_OPEN_FOR_BID_state() throws ContractNotExists {
        createContract_in_dao_with_state(ContractState.OPEN_FOR_BID, true);
        ContractDto contract = bidService.getContract(ContractTestUtil.contractID);
        assertContractDtoFields(contract, ContractState.OPEN_FOR_BID);
        assertThat(contract.getBidCount(), is(notNullValue()));
    }

    @Test
    public void test_return_contract_with_CLOSE_FOR_BID_state() throws ContractNotExists {
        createContract_in_dao_with_state(ContractState.CLOSE_FOR_BID, true);
        ContractDto contract = bidService.getContract(ContractTestUtil.contractID);
        assertContractDtoFields(contract, ContractState.CLOSE_FOR_BID);
        assertThat(contract.getBidCount(), is(notNullValue()));
    }

    @Test
    public void test_return_contract_with_ASSIGNED_state() throws ContractNotExists {
        createContract_in_dao_with_state(ContractState.ASSIGNED, true,true);
        ContractDto contract = bidService.getContract(ContractTestUtil.contractID);
        assertContractDtoFields(contract, ContractState.ASSIGNED);
        assertThat(contract.getBidCount(), is(notNullValue()));
        assertThat(contract.getAssignedDate(), is(notNullValue()));
        assertThat(contract.getContractor(), is(notNullValue()));
    }

    @Test
    public void test_return_contract_with_BID_FAIL_state() throws ContractNotExists {
        createContract_in_dao_with_state(ContractState.BID_FAIL, true,false);
        ContractDto contract = bidService.getContract(ContractTestUtil.contractID);
        assertContractDtoFields(contract, ContractState.BID_FAIL);
        assertThat(contract.getBidCount(), is(notNullValue()));
        assertThat(contract.getAssignedDate(), is(nullValue()));
        assertThat(contract.getContractor(), is(nullValue()));
    }



    private void assertContractDtoFields(ContractDto contractDto, ContractState status) {
        assertThat(contractDto.getState(), is(equalTo(status.toString())));
        assertThat(contractDto.getBidEndDate(), is(notNullValue()));
        assertThat(contractDto.getBidStartDate(), is(notNullValue()));
        assertThat(contractDto.getContractID(), is(notNullValue()));
        assertThat(contractDto.getDescription(), is(notNullValue()));
        assertThat(contractDto.getMinimumBid(), is(notNullValue()));
    }

    private void set_Current_Date_for_contract_assignment_event() {
     CurrentDateProvider.setDate(LocalDate.of(2019,11,12));
    }

    private void createContract_in_dao_with_state(ContractState contractState, boolean ...dataSetting) {
        boolean isBidData = dataSetting[0];
        boolean isAssignedData = dataSetting.length<=1?false:dataSetting[1];

        SimpleContract.ContractBuilder builder =  ContractTestUtil.getContractBuilderWithState(contractState);
        if(isBidData)
           builder.setBidders(ContractTestUtil.getBidList());



        if(isAssignedData) {
            Contractor contractor = new Contractor(ContractTestUtil.contractID);
            AllotementData allotementData = AllotementData.getAllotementData(contractor, LocalDate.now());
            builder.setAllotement(allotementData);
        }


        Contract contract = builder.build();
        dao.addContract(contract);
    }

    private ContractDto createContract1() throws DuplicateContractException {
        return bidService.createContract(ContractTestUtil.contractID,ContractTestUtil.BID_START_DATE,
                ContractTestUtil.BID_END_DATE, ContractTestUtil.DESCRIPTION, "500");
    }

    private void changeCurrentDateGreaterThanBidStartDate() {

        CurrentDateProvider.setDate(LocalDate.of(2019,10,02));
    }

    private void changeCurrentDateEqualToBeforeThanBidStartDate() {
        CurrentDateProvider.setDate(LocalDate.of(2019,10,01));
    }

    private void changeCurrentDateAfterBidEndDate() {

        CurrentDateProvider.setDate(LocalDate.of(2019,11,02));
    }

    private void changeCurrentDateEqualToBeforeThanBidEndDate() {
       CurrentDateProvider.setDate(LocalDate.of(2019,10,30));
    }

}
