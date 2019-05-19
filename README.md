# my-bidnetwork
We often see the lack of transparency and bad practices in the bidding process due to manual intervention. Now we can implement the whole bidding process with block chain technology to improve the transparency and manual intervention to very high limit.

This POC is implemented in the blockchain framework hyperledger(1.4) which provides many good things over public block chain.

Hyperledger framework allows

1) High transaction throughput.
2) We can add the nodes based on their role and restrict their functionality according to role.
3) It allows sharing of private data between two nodes allowing confidentiality between the two organization.

I have used above characteristics of the hyperledger to implement the this POC.


# Use Case Scenario Overview: Bidding for Goverment Contract

![alt text](images/organization.jpg)

This use case shows how bidding process for government contract can be implemented using blockchain. In this use cases we have four organization which are participating in the bid network each on of having its specific role.

1) GOV: GOV will add the contract in the blockchain having the information as contractID, bidStartDate, bidEndDate, description, minimumBid. For this contract GOV is seeking highest bidder.
2) REGULATOR: This participant will act as regulator for bidding process. All the newly created blocks will be distributed from this node.
3) Contractor1,Contractor2: These participants will bid for the government contract added by the GOV.
All the bidding events will be captured on the blockchain with consensus mechanism so that information will be trusted by each participants. Details of the whole process is as follows.

To implement the bidding process on blockchain, we can convert contract to be bid as entity and deploy it on blockchain. Then we can tack the lifestyle of the contract in transparent non correctable way. In each stage of the contract lifecycle, smart contract(chaincode) can perform the different action. Following contract lifecycle is implemented for this POC

# Contract Lifecycle:

Following are the stages in the contract lifcycle deployed in blockchain. All the movement in the contract lifcycle is based on the business rules implemented chaincode.

1) NEW: This is the state when new contract is created by the GOV organization. This is the initial stage of contract and can be used to propagate the information to the different contractor about the contract details. Initiater for this stage is GOV
2) OPEN_FOR_BID: After the initial cooling period, the contract is open for bid. At this stage different contractors are can bid for the contract.  Chaincode will validate the incoming bid and either accept/reject bid based on business validation implemented. Initiater for this stage is REGULATOR.
3) CLOSE_FOR_BID: At this stage the contract is closed for bidding, no new bid will be accepted. Initiater for this stage is REGULATOR. 
4) Assign: At this stage contract is assigned to the highest bidder. If their are bids present for the contracts then highest bidder is selected and assigned the contract, if non of the bid is present then contract will move BID_FAIL stage.




![alt text](images/contract_lifecycle.jpg)


# Testing and Running the Application

Following are the steps to run the bidnetwork application.

Basic setup is required to run this hyperledger application. The basic setup and can implemented using follwing link.

https://hyperledger-fabric.readthedocs.io/en/release-1.4/build_network.html 

Once the basic network is running, you can run the follwing steps to run the bidnetwork.

Move to the root folder my-bidnetwork and execute the following steps.

1) Generate the crypto material for all the participants using following command.

    **./generateartifact.sh**
    
    After the execution of the above statement follwing files are created.
    
     channel-artifacts<br />
      - channel.tx <br />
      - Contractor1MSPanchors.tx <br />
      - Contractor2MSPanchors.tx <br />
      - genesis.block <br />
      - GovMSPanchors.tx <br />
      - RegulatorMSPanchors.tx <br />
      
     crypto-config <br />
      - ordererOrganizations<br />
      - peerOrganizations<br />
       -- contractor1.example.com<br />
       -- contractor2.example.com<br />
       -- gov.example.com<br />
       -- regulator.example.com<br />
                                 

2) Start the bid network .

     **./start.sh**
    After the successfull execution of above script. The following container will be up. check using command **docker ps**
    
    ![alt text](images/docker_ps.png)


3) Running the integration tests.

Now you can run the integration test, which will run happy flow creating, bidding and assigning the contract.

Run the following methods from the class [BidNetworkIntegrationTest](bidclientblockchain/src/test/java/bid/client/blockchain/BidNetworkIntegrationTest.java)

| Test | Description |Expected Result|Block Number Added|
| --- | --- | --- | --- |
| initializeContainers | This test initialize all the chaincode containers of all participants |  No Response |No Block|
| create_new_gov_contract| This test will create the new contract on blockchain.|message: "Created Contract Successfully" from all 4 peers|Block No- 7|
|query_bid_contract|query blockchain to fetch the contract data|message: "Contract present in the ledger"|No Block|
|change_state_of_the_contract_open_for_bid|change the state OPEN_FOR_BID| "Contract: PWD-12-9024 is moved to the state :OPEN_FOR_BID" |Block No- 8|
|test_add_bid_from_Contractor_1|add bid for contractor1|message: "Bid added for the Contract with idPWD-12-9024"|Block No- 9|
|test_add_bid_from_Contractor_2|add bid for contractor2|message: "Bid added for the Contract with idPWD-12-9024"|Block No- 10|
|change_state_of_the_contract_close_for_bid|changes the state CLOSE_FOR_BID|message: "Contract: PWD-12-9024 is moved to the state :CLOSE_FOR_BID"|Block No- 11|
|allocate_contract|allocate contract process|message: "Contract: PWD-12-9024 is moved to the state :ASSIGNED"|Block No- 12|


**Initial state of the contract on first test:create_new_gov_contract: BLOCK-7**<br/>
 "{\"contractID\":\"PWD-12-9024\",\"bidStartDate\":\"01/10/2019\",\"bidEndDate\":\"01/11/2019\",\"description\":\"PWD contract\",\"state\":\"NEW\",\"minimumBid\":\"500\",\"bidCount\":0}"
 
**State of the contract after last test: allocate_contract: BLOCK-11**<br/>
"{\"contractID\":\"PWD-12-9024\",\"bidStartDate\":\"01/10/2019\",\"bidEndDate\":\"01/11/2019\",\"description\":\"PWD contract\",\"state\":\"ASSIGNED\",\"minimumBid\":\"500\",\"bidCount\":2,\"contractor\":\"contractor1\",\"assignedDate\":\"2019-05-18\"}"

4) Stop the netowrk: Network is stopped using script [./stop.sh](stop.sh), this script will bring down all the containers and remove all the docker volume. Very important step before starting the network next time

# Application Architecture



Application Architecture has the folloiwng three main component:

- **Fabrc Bid Network**
- **Fabric Chaincode**
- **Fabric java client**

## Fabric Bid Network

![alt text](images/architecture_2.jpg)


Following componets are required to create Bid network.

- [crypto-config.yaml](crypto-config.yaml) : This file is used by the file [generateartifact.sh](generateartifact.sh) to create the all the crypto material required for the network.
- [configtx.yaml](configtx.yaml) : This file will be used by the file [generateartifact.sh](generateartifact.sh) to create all the initial blocks(genesis,channel, achor peer) to start the network.
- [docker-compose-cli.yaml](docker-compose-cli.yaml) : This file is used by the [start.sh](start.sh) to start the docker containers for each participants. File docker-compose-cli.yaml uses the [peer-base.yaml](base/peer-base.yaml) and [docker-compose-base.yaml](base/docker-compose-base.yaml) to build container network.

Some of the features of bid newtowrk
- For each participant only one node is created which is also acting as achor peer.<br/>
- Network is not using any CA servers.<br/>
- Network is operating withou tls.<br/>
- Network uses the solo orderer


