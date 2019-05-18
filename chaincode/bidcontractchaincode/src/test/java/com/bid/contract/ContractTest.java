package com.bid.contract;





import com.bid.contract.exception.BidRejectException;
import com.bid.contract.exception.NextStateException;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;


import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import com.bid.util.ContractTestUtil;
import static com.bid.util.ContractTestUtil.getBidList;
import static com.bid.util.ContractTestUtil.getBid;
import static com.bid.util.ContractTestUtil.DESCRIPTION;
import static com.bid.util.ContractTestUtil.contractID;


public class ContractTest {

    private Contract contract = null;

    @Before
    public void init() {

        contract = ContractTestUtil.getContractBuilderWithState(ContractState.NEW)
                .build();
    }

    @Test
    public void createSimpleContractWith_NEW_State() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.NEW)
                .build();
        assertThat(simpleContract.getState(), is(equalTo(ContractState.NEW)));
        checkContractData(simpleContract);
    }

    @Test
    public void createSimpleContractWith_NEW_State_with_Bidders() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.NEW)
                .setBidders(getBidList())
                .build();
        assertThat(simpleContract.getBidCount(), is(equalTo(0)));
    }

    @Test(expected = RuntimeException.class)
    public void createSimpleContractWith_NEW_State_with_Alloters() {
     Contract simpleContract =    ContractTestUtil.getContractBuilderWithState(ContractState.NEW)
                .setAllotement(getAllotementData())
                .build();
        simpleContract.getAllottedDate();
    }


    @Test
    public void createSimpleContractWith_OPEN_OF_BID_status() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.OPEN_FOR_BID)
                .build();
        assertThat(simpleContract.getState(), is(equalTo(ContractState.OPEN_FOR_BID)));
        checkContractData(simpleContract);
    }

    @Test
    public void createSimpleContractWith_OPEN_OF_BID_status_with_bidders() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.OPEN_FOR_BID)
                .setBidders(getBidList())
                .build();
        assertThat(simpleContract.getState(), is(equalTo(ContractState.OPEN_FOR_BID)));
        checkContractData(simpleContract);
        assertThat(simpleContract.getBidCount(), is(1));
    }

    @Test(expected = RuntimeException.class)
    public void createSimpleContractWith_OPEN_FOR_BID_status_with_bidders_and_allotement() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.OPEN_FOR_BID)
                .setBidders(getBidList())
                .setAllotement(getAllotementData())
                .build();
        simpleContract.getAllottedDate();

    }


    @Test
    public void createSimpleContract_with_CLOSE_FOR_BID() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.CLOSE_FOR_BID)
                .build();
        assertThat(simpleContract.getState(), is(equalTo(ContractState.CLOSE_FOR_BID)));
        checkContractData(simpleContract);
        assertThat(simpleContract.getBidCount(), is(0));
    }


    @Test
    public void createSimpleContract_with_CLOSE_FOR_BID_with_bidders() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.CLOSE_FOR_BID)
                .setBidders(getBidList())
                .build();
        assertThat(simpleContract.getState(), is(equalTo(ContractState.CLOSE_FOR_BID)));
        checkContractData(simpleContract);
        assertThat(simpleContract.getBidCount(), is(1));
    }


    @Test(expected=RuntimeException.class)
    public void createSimpleContract_with_CLOSE_FOR_BID_with_bidders_allotment() {
      Contract simpleContract =  ContractTestUtil. getContractBuilderWithState(ContractState.OPEN_FOR_BID)
                .setBidders(getBidList())
                .setAllotement(getAllotementData())
                .build();
      simpleContract.getAllottedContractor();

    }


    @Test
    public void createSimpleContract_with_ASSIGNED_state() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.ASSIGNED)
                .setBidders(getBidList())
                .setAllotement(getAllotementData())
                .build();
        assertThat(simpleContract.getState(), is(equalTo(ContractState.ASSIGNED)));
        checkContractData(simpleContract);
        assertThat(simpleContract.getAllottedContractor(), is(notNullValue()));
    }

    @Test
    public void creteSimpleContract_with_ASSIGNED_without_bidder_and_assignment() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.ASSIGNED)
                .build();
        assertThat(simpleContract.getState(), is(equalTo(ContractState.ASSIGNED)));
        checkContractData(simpleContract);
        assertThat(simpleContract.getAllottedContractor(), is(nullValue()));
    }


    @Test
    public void createSimpleContract_with_BID_FAIL() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.BID_FAIL)
                .build();
        assertThat(simpleContract.getState(), is(equalTo(ContractState.BID_FAIL)));
        checkContractData(simpleContract);
    }

    @Test
    public void createSimpleContract_with_BID_FAIL_with_bidders() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.BID_FAIL)
                .setBidders(getBidList())
                .build();
        assertThat(simpleContract.getState(), is(equalTo(ContractState.BID_FAIL)));
        checkContractData(simpleContract);
        assertThat(simpleContract.getBidCount(), is(equalTo(1)));
    }

    @Test
    public void createSimpleContract_with_BID_FAIL_with_bidders_allotment() {
        Contract simpleContract = ContractTestUtil.getContractBuilderWithState(ContractState.BID_FAIL)
                .setBidders(getBidList())
                .setAllotement(getAllotementData())
                .build();
        assertThat(simpleContract.getState(), is(equalTo(ContractState.BID_FAIL)));
        checkContractData(simpleContract);
        assertThat(simpleContract.getBidCount(), is(equalTo(1)));
    }

    @Test
    public void check_created_contract_in_new_state() {
        assertThat(contract.getState(), is(equalTo(ContractState.NEW)));
    }

    @Test
    public void change_the_contract_state_on_bidding_day() throws NextStateException {
        ContractState state = changeContractToBiddingState();
        assertThat(state, is(equalTo(ContractState.OPEN_FOR_BID)));
    }

    @Test(expected = NextStateException.class)
    public void error_when_move_to_next_state_before_bid_start_date() throws NextStateException {
        CurrentDateProvider.setDate(LocalDate.of(2019, 11, 02));
        ContractState state = contract.nextState();
        assertThat(state, is(equalTo(ContractState.CLOSE_FOR_BID)));
    }

    @Test
    public void given_contract_in_OPEN_FOR_BID_state_add_new_bid_for_the_contract() throws BidRejectException,
            NextStateException {
        changeContractToBiddingState();
        Contractor contractor1 = new Contractor("contractor1");
        Contractor contractor2 = new Contractor("contractor2");
        BigDecimal quote = new BigDecimal(894.45);
        Bid bid = new Bid(contractor1, quote);
        contract.acceptBid(bid);
        assertThat(contract.getBidCount(), is(equalTo(1)));
        BigDecimal quote2 = new BigDecimal(800);
        Bid bid2 = new Bid(contractor2, quote2);
        contract.acceptBid(bid2);
        assertThat(contract.getBidCount(), is(equalTo(2)));

    }


    @Test(expected = BidRejectException.class)
    public void reject_New_Bid_When_Quote_Is_below_Minimum_Bid() throws BidRejectException {
        Contractor contractor = new Contractor("contractor1");
        BigDecimal quote = new BigDecimal(200);
        Bid bid = new Bid(contractor, quote);
        contract.acceptBid(bid);
    }

    @Test
    public void given_contract_OPEN_BID_STATE_next_state_is_close_bid() throws NextStateException {
       ContractState state = changeContractToCloseForBiddingState(false);
        assertThat(state, is(equalTo(ContractState.CLOSE_FOR_BID)));

    }

    @Test(expected = BidRejectException.class)
    public void reject_bid_when_contract_CLOSE_BID_STATE_state() throws NextStateException, BidRejectException {
        changeContractToCloseForBiddingState(false);
        Contractor contractor = new Contractor("contractor1");
        BigDecimal quote = new BigDecimal(200);
        Bid bid = new Bid(contractor, quote);
        contract.acceptBid(bid);
    }

    @Test
    public void given_state_CLOSE_FOR_BID__move_to_next_step_if_valid_bid_present() throws NextStateException {
        changeContractToCloseForBiddingState(true);
        ContractState state = contract.nextState();
        assertThat(state, is(equalTo(ContractState.ASSIGNED)));
        assertThat(contract.getAllottedContractor(), is(notNullValue()));
        assertThat(contract.getAllottedDate(), is(notNullValue()));
    }

    @Test
    public void given_state_CLOSE_FOR_BID__move_to_next_step_if_valid_bid_is_not_present() throws NextStateException {
        ContractState state = changeContractToBidFailState();
        assertThat(state, is(equalTo(ContractState.BID_FAIL)));
    }


    @Test(expected = BidRejectException.class)
    public void reject_duplicate_bid() throws BidRejectException, NextStateException {
        changeContractToBiddingState();
        try {
            createNewBid();
        } catch (BidRejectException e) {
            assertThat("Bid Should be accpeted " ,false);
        }

        createNewBid();
    }

    @Test
    public void createNewContractObject() {
        assertThat(contract.getContractId().getId(),is(equalTo(contractID)));
        assertThat(contract.getBidStartDate(),is(equalTo(LocalDate.of(2019, 10,1))));
        assertThat(contract.getBidEndDate(),is(equalTo(LocalDate.of(2019, 11,1))));
        assertThat(contract.getDescription(),is(equalTo(DESCRIPTION)));
        assertThat(contract.getMinimumBid(),is(equalTo(new BigDecimal(500))));
    }

    @Test
    public void contractStateNew_WhenNewContractCreated() {
        assertThat(contract.getState(), is(equalTo(ContractState.NEW)));
    }


    private ContractState changeContractToBidFailState() throws NextStateException {
        changeContractToCloseForBiddingState(false);
        return contract.nextState();
    }



    private void checkContractData(Contract simpleContract) {
        assertThat(simpleContract.getContractId(), is(notNullValue()));
        assertThat(simpleContract.getBidStartDate(), is(notNullValue()));
        assertThat(simpleContract.getBidEndDate(), is(notNullValue()));
        assertThat(simpleContract.getMinimumBid(), is(notNullValue()));
    }

    private ContractState changeContractToBiddingState() throws NextStateException {
        CurrentDateProvider.setDate(LocalDate.of(2019, 10, 6));
        return contract.nextState();
    }

    private ContractState changeContractToCloseForBiddingState(boolean withValidBids) throws NextStateException {
        changeContractToBiddingState();
        if(withValidBids) {
           createNewBids();
        }
        CurrentDateProvider.setDate(LocalDate.of(2019, 11, 2));
        return contract.nextState();
    }



    private void createNewBids() {
        createNewBid_for_allotContractToBidder1();
        createNewBid_for_allotContractToBidder2();
    }


    private void createNewBid() throws BidRejectException {
        Bid bid = getBid("contractor1", 800);
        contract.acceptBid(bid);
    }

    private void createNewBid_for_allotContractToBidder1() {

        Bid bid = getBid("contractor1", 800);
        try {
            contract.acceptBid(bid);
        } catch (BidRejectException e) {
            e.printStackTrace();
        }
    }

    private void createNewBid_for_allotContractToBidder2() {
        Bid bid = getBid("contractor2", 700);
        try {
            contract.acceptBid(bid);
        } catch (BidRejectException e) {
            e.printStackTrace();
        }
    }


    private AllotementData getAllotementData() {
        Contractor contractor = new Contractor("contracor 1");
        LocalDate allotementDate = LocalDate.now();
        return AllotementData.getAllotementData(contractor, allotementDate);
    }


}
