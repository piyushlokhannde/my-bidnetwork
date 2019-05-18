package bid.client.blockchain;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.junit.Test;


import java.util.Collection;

import static bid.client.blockchain.BidNetWorkHelper.*;

public class BidNetworkIntegrationTest {

    private static Log log = LogFactory.getLog(BidNetworkIntegrationTest.class);

    /*This test is written to initialize the chaincode as it take time start chaincode container*/
    @Test
    public void initializeContainers() {
         for(Participant participant: Participant.values()) {
             try {
                 HFClient client = crateHFClient(participant);
                 Channel channel = crateChannelWithParticipants(client, participant);
                 queryBidChannel(client, channel, "PWD-12-9024", "getcontract");
             } catch (Exception e) {
                 log.error(e.getMessage());
             }

         }
    }



    @Test
    public void create_new_gov_contract() throws Exception {
        Participant participant = Participant.GOV;
        HFClient client = crateHFClient(participant);
        Channel channel = crateChannelWithParticipants(client,
                Participant.GOV, Participant.REGULATOR, Participant.CONTRACTOR1, Participant.CONTRACTOR2);
        Collection<Peer> peerList = channel.getPeers();
        peerList.stream().forEach(peer -> System.out.println(peer.getName()));
        String[] args =  new String[] {"PWD-12-9024", "01/10/2019", "01/11/2019", "PWD contract", "500"};
        BlockEvent.TransactionEvent transactionEvent =
                invokeChaincode(client, channel, "createcontract" , args, BIDCC);

    }

    @Test
    public void query_bid_contract() throws Exception {
        Participant participant = Participant.GOV;
        HFClient client = crateHFClient(participant);
        Channel channel = crateChannelWithParticipants(client, participant);
        queryBidChannel(client, channel, "PWD-12-9024", "getcontract");

    }


    @Test
    public void change_state_of_the_contract_open_for_bid() throws Exception {
        Participant participant = Participant.GOV;
        HFClient client = crateHFClient(participant);
        Channel channel = crateChannelWithParticipants(client,
                Participant.GOV, Participant.REGULATOR, Participant.CONTRACTOR1, Participant.CONTRACTOR2);
        Collection<Peer> peerList = channel.getPeers();
        peerList.stream().forEach(peer -> System.out.println(peer.getName()));
        String[] args =  new String[] {"PWD-12-9024"};
        BlockEvent.TransactionEvent transactionEvent =
                invokeChaincode(client, channel, "movenextstate" , args, BIDCC);

    }


    @Test
    public void test_add_bid_from_Contractor_1()  throws Exception {
        Participant participant = Participant.CONTRACTOR1;
        HFClient client = crateHFClient(participant);
        Channel channel = crateChannelWithParticipants(client,
                Participant.GOV, Participant.REGULATOR, Participant.CONTRACTOR1);
        Collection<Peer> peerList = channel.getPeers();
        peerList.stream().forEach(peer -> System.out.println(peer.getName()));
        String[] args =  new String[] {"PWD-12-9024", "contractor1", "900"};
        BlockEvent.TransactionEvent transactionEvent =
                invokeChaincode(client, channel, "addbid" , args, BIDCC);

    }

    @Test
    public void test_add_bid_from_Contractor_2()  throws Exception {
        Participant participant = Participant.CONTRACTOR2;
        HFClient client = crateHFClient(participant);
        Channel channel = crateChannelWithParticipants(client,
                Participant.GOV, Participant.REGULATOR, Participant.CONTRACTOR2);
        Collection<Peer> peerList = channel.getPeers();
        peerList.stream().forEach(peer -> System.out.println(peer.getName()));
        String[] args =  new String[] {"PWD-12-9024", "contractor2", "800"};
        BlockEvent.TransactionEvent transactionEvent =
                invokeChaincode(client, channel, "addbid" , args, BIDCC);

    }


    @Test
    public void change_state_of_the_contract_close_for_bid() throws Exception {
        Participant participant = Participant.GOV;
        HFClient client = crateHFClient(participant);
        Channel channel = crateChannelWithParticipants(client,
                Participant.GOV, Participant.REGULATOR, Participant.CONTRACTOR1, Participant.CONTRACTOR2);
        Collection<Peer> peerList = channel.getPeers();
        peerList.stream().forEach(peer -> System.out.println(peer.getName()));
        String[] args =  new String[] {"PWD-12-9024"};
        BlockEvent.TransactionEvent transactionEvent =
                invokeChaincode(client, channel, "movenextstate" , args, BIDCC);

    }


    @Test
    public void allocate_contract() throws Exception {
        Participant participant = Participant.GOV;
        HFClient client = crateHFClient(participant);
        Channel channel = crateChannelWithParticipants(client,
                Participant.GOV, Participant.REGULATOR);
        Collection<Peer> peerList = channel.getPeers();
        peerList.stream().forEach(peer -> System.out.println(peer.getName()));
        String[] args =  new String[] {"PWD-12-9024"};
        BlockEvent.TransactionEvent transactionEvent =
                invokeChaincode(client, channel, "assingcontract" , args, BIDCC);

    }

}
