# my-bidnetwork
We often see the lack of transparency and bad practices in the bidding process due to manual intervention. Now we can implement the whole bidding process with block chain technology to improve the transparency and manual intervention to very high limit.

This POC is implemented in the blockchain framework hyperledger(1.4) which provides many good things over public block chain.

Hyperledger framework allows

1) High transaction throughput.
2) We can add the nodes based on their role and restrict their functionality according to role.
3) It allows sharing of private data between two nodes allowing confidentiality between the two organization.

I have used above characteristics of the hyperledger to implement the this POC.


# Use Case Scenario Overview: bidding for goverment contract

![alt text](images/organization.jpg)

This use case shows how bidding process for government contract can be implemented using blockchain. In this use cases we have four organization which are participating in the bid network each on of having its specific role.

GOV: GOV will add the contract in the blockchain having the information as contractID, bidStartDate, bidEndDate, description, minimumBid. For this contract GOV is seeking highest bidder.
REGULATOR: This participant will act as regulator for bidding process. All the newly created blocks will be distributed from this node.
Contractor1,Contractor2: These participants will bid for the government contract added by the GOV.
All the bidding events will be captured on the blockchain with consensus mechanism so that information will be trusted by each participants. Details of the whole process is as follows.


