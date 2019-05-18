package bid.client.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BidNetWorkHelper {

    public static final String BIDCC = "bidcc";
    public static final String CHANNEL_NAME = "bidchannel";
    public static final String ALLOCATE_CC = "allocatecc";
    private static Logger log = LoggerFactory.getLogger(BidNetWorkHelper.class);


    public static HFClient crateHFClient(Participant participant) throws CryptoException,
            InvalidArgumentException, ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        HFClient client = HFClient.createNewInstance();
        CryptoSuite cs = CryptoSuite.Factory.getCryptoSuite();
        client.setCryptoSuite(cs);
        AppUser user = new AppUser(participant);
        client.setUserContext(user);
        return client;
    }


    public static Channel crateChannelWithParticipants(HFClient client, Participant... participants)
            throws TransactionException, InvalidArgumentException {
        Channel channel = client.newChannel(CHANNEL_NAME);

        for (Participant participant: participants ) {
            channel.addPeer(createPeer(participant, client));
        }

       /* channel.addPeer(createPeer(Participant.GOV, client));
        channel.addPeer(createPeer(Participant.REGULATOR, client));
        channel.addPeer(createPeer(Participant.CONTRACTOR1, client));
        channel.addPeer(createPeer(Participant.CONTRACTOR2, client));*/
        channel.addOrderer(crateOrderer(client));
        channel.initialize();
        return channel;
    }


    public static ProposalResponse queryBidChannel(HFClient client, Channel channel, String key, String function) throws
            InvalidArgumentException, ProposalException, ProposalResponseException {
        QueryByChaincodeRequest req = client.newQueryProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName(BIDCC).build();
        req.setChaincodeID(cid);
        req.setFcn(function);
        req.setArgs(new String[] { key });
        log.info("Querying for " + key);
        Collection<ProposalResponse> resps = channel.queryByChaincode(req);
        handleProposalResponse(resps);
        return resps.stream().findFirst().get();
    }



    public static  BlockEvent.TransactionEvent invokeChaincode(HFClient client, Channel channel, String function,
                                                               String[] args, String chainCodeName)
            throws InvalidArgumentException, ProposalException,
            ExecutionException, InterruptedException, ProposalResponseException {

        TransactionProposalRequest req = client.newTransactionProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName(chainCodeName).build();
        req.setChaincodeID(cid);
        req.setFcn(function);
        req.setArgs(args);
        Collection<ProposalResponse> resps = channel.sendTransactionProposal(req);
        handleProposalResponse(resps);

        CompletableFuture<BlockEvent.TransactionEvent>  finalResult = channel.sendTransaction(resps);

        finalResult.thenApply(transactionEvent ->
        {log.info("Result: " + transactionEvent.getBlockEvent().getBlockNumber());
        return transactionEvent;})
                .exceptionally(e -> {
                    System.err.println("Error greeting: " + e.getMessage());
                    return null;
                });

         while (!finalResult.isDone()) {
             System.out.println("Waiting for the result");
             TimeUnit.SECONDS.sleep(7);
         }

        BlockEvent.TransactionEvent transactionEvent = finalResult.get();
        return transactionEvent;

    }

    private static void handleProposalResponse(Collection<ProposalResponse> responses) throws ProposalResponseException {

        List<String> errorList = new ArrayList<>();

        for(ProposalResponse proposalResponse: responses) {
            String resp = "Peer: "+ proposalResponse.getPeer()+ " :: "+ " Response: "+ proposalResponse.getProposalResponse().getResponse();

            if(proposalResponse.isInvalid()) {
                log.error(resp);
                errorList.add(resp);
            } else  {
                log.info(resp);
            }
        }

        if(!errorList.isEmpty()) {
            throw new ProposalResponseException(errorList);
        }

    }


    private static Peer createPeer(Participant participant, HFClient client) throws InvalidArgumentException {
        String peerName = "peer0."+participant.name;
        Peer peer =  client.newPeer(peerName, participant.URL);
        return peer;
    }

    private static Orderer crateOrderer(HFClient client) throws InvalidArgumentException {
      /*  Properties orderer_properties = new Properties();
        orderer_properties.put("pemBytes", Participant.ORDERER.getTlsCert());
        orderer_properties.setProperty("sslProvider", "openSSL");
        orderer_properties.setProperty("negotiationType", "TLS");*/
        Orderer orderer = client.newOrderer("orderer.example.com", Participant.ORDERER.URL);
        return orderer;
    }
}
