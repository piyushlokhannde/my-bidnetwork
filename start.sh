DELAY="2"
SMALLDELAY="1"
ORGS=("GovMSP" "RegulatorMSP" "Contractor1MSP" "Contractor2MSP")



CHANNEL_NAME=bidchannel
IMAGETAG="latest"
IMAGE_TAG="latest"
IMAGETAG="latest"

CORE_PEER_LOCALMSPID="CORE_PEER_LOCALMSPID"
CORE_PEER_TLS_ROOTCERT_FILE="CORE_PEER_TLS_ROOTCERT_FILE"
CORE_PEER_MSPCONFIGPATH="CORE_PEER_MSPCONFIGPATH"
CORE_PEER_ADDRESS="CORE_PEER_ADDRESS"
CORE_PEER_ANCHOR="CORE_PEER_ANCHOR"



ORDERER_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
GovMSP_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/gov.example.com/peers/peer0.gov.example.com/tls/ca.crt 
RegulatorMSP_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/regulator.example.com/peers/peer0.regulator.example.com/tls/ca.crt
Contractor1MSP_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/contractor1.example.com/peers/peer0.contractor1.example.com/tls/ca.crt
Contractor2MSP_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/contractor2.example.com/peers/peer0.contractor2.example.com/tls/ca.crt

# verify the result of the end-to-end test
verifyResult() {
  if [ $1 -ne 0 ]; then
    echo "!!!!!!!!!!!!!!! "$2" !!!!!!!!!!!!!!!!"
    echo "========= ERROR !!! FAILED to execute End-2-End Scenario ==========="
    echo
    exit 1
  fi
}


setEnvironment() {
  PEER=$1
  ORG="$2"

  if [ $ORG == "GovMSP" ]; then
    CORE_PEER_LOCALMSPID="GovMSP"
    CORE_PEER_TLS_ROOTCERT_FILE=$GovMSP_CA
    CORE_PEER_ANCHOR=GovMSPanchors.tx
    CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/gov.example.com/users/Admin@gov.example.com/msp
    if [ $PEER -eq 0 ]; then
      CORE_PEER_ADDRESS=peer0.gov.example.com:7051
    else
      CORE_PEER_ADDRESS=peer1.gov.example.com:8051
    fi
  elif [ $ORG == "RegulatorMSP" ]; then
    CORE_PEER_LOCALMSPID="RegulatorMSP"
    CORE_PEER_TLS_ROOTCERT_FILE=$RegulatorMSP_CA
    CORE_PEER_ANCHOR=RegulatorMSPanchors.tx
    CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/regulator.example.com/users/Admin@regulator.example.com/msp
    if [ $PEER -eq 0 ]; then
      CORE_PEER_ADDRESS=peer0.regulator.example.com:8051
    else
      CORE_PEER_ADDRESS=peer1.regulator.example.com:10051
    fi

  elif [ $ORG == "Contractor1MSP" ]; then
    CORE_PEER_LOCALMSPID="Contractor1MSP"
    CORE_PEER_TLS_ROOTCERT_FILE=$Contractor1MSP_CA
    CORE_PEER_ANCHOR=Contractor1MSPanchors.tx
    CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/contractor1.example.com/users/Admin@contractor1.example.com/msp
    if [ $PEER -eq 0 ]; then
      CORE_PEER_ADDRESS=peer0.contractor1.example.com:9051
    else
      CORE_PEER_ADDRESS=peer1.contractor1.example.com:12051
    fi
  elif [ $ORG == "Contractor2MSP" ]; then
    CORE_PEER_LOCALMSPID="Contractor2MSP"
    CORE_PEER_TLS_ROOTCERT_FILE=$Contractor2MSP_CA
    CORE_PEER_ANCHOR=Contractor2MSPanchors.tx
    CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/contractor2.example.com/users/Admin@contractor2.example.com/msp
    if [ $PEER -eq 0 ]; then
      CORE_PEER_ADDRESS=peer0.contractor2.example.com:10051
    else
      CORE_PEER_ADDRESS=peer1.contractor2.example.com:14051
    fi
  else
    echo "================== ERROR !!! ORG Unknown =================="
  fi

  if [ "$VERBOSE" == "true" ]; then
    env | grep CORE
  fi
}






start_bid_network() {

 echo "========Starting bid network ============="
 set -x
  echo $IMAGE_TAG
  IMAGE_TAG=$IMAGETAG  COMPOSE_PROJECT_NAME=mybidnetwork docker-compose -f ./docker-compose-cli.yaml up -d	
  res=$?
 set +x
 sleep 10
 verifyResult $res "Failed to start the bidnetwork "
 echo
}


create_channel() {
 set -x
  docker exec cli peer channel create -o orderer.example.com:7050 -c $CHANNEL_NAME -f /opt/gopath/src/github.com/hyperledger/fabric/peer/channel-artifacts/channel.tx
  res=$? 
 set +x
 sleep $SMALLDELAY
 verifyResult $res "Failed to create the bid channel "
 echo 
}


add_peers_to_channel() {
   for ORG in ${ORGS[@]}; do
    for peer in 0; do		
        setEnvironment $peer $ORG
       #echo  $CORE_PEER_LOCALMSPID
       #echo $CORE_PEER_TLS_ROOTCERT_FILE
       #echo $CORE_PEER_MSPCONFIGPATH
       #echo $CORE_PEER_ADDRESS
       joinchannel $peer $ORG 
       sleep $DELAY
     done    
   done
}


joinchannel() {
  PEER=$1
  ORG="$2"
  echo "=====================Joining bidchannel for ORG=$ORG and PEER=$CORE_PEER_ADDRESS  ===================== "
  set -x
    docker exec -e "CORE_PEER_LOCALMSPID=$CORE_PEER_LOCALMSPID" -e "CORE_PEER_TLS_ROOTCERT_FILE=$CORE_PEER_TLS_ROOTCERT_FILE" -e   "CORE_PEER_MSPCONFIGPATH=$CORE_PEER_MSPCONFIGPATH" -e "CORE_PEER_ADDRESS=$CORE_PEER_ADDRESS" cli peer channel join -b $CHANNEL_NAME.block
  res=$? 
  set +x
  sleep $DELAY
  verifyResult $res "Failed to join the bid channel  for ORG=$ORG and PEER=$PEER"
  echo 
}

update_anchor_peers() {
   for ORG in ${ORGS[@]}; do
    for peer in 0; do		
        setEnvironment $peer $ORG
       #echo  $CORE_PEER_LOCALMSPID
       #echo $CORE_PEER_TLS_ROOTCERT_FILE
       #echo $CORE_PEER_MSPCONFIGPATH
       #echo $CORE_PEER_ADDRESS
       updateAnchorForPeer $peer $ORG 
       sleep $DELAY
     done    
   done
}

updateAnchorForPeer() {
  PEER=$1
  ORG="$2"
  echo "=====================Updating for anchor peers for ORG=$ORG and PEER=$CORE_PEER_ADDRESS  ===================== "
  set -x
    docker exec -e "CORE_PEER_LOCALMSPID=$CORE_PEER_LOCALMSPID" -e "CORE_PEER_TLS_ROOTCERT_FILE=$CORE_PEER_TLS_ROOTCERT_FILE" -e   "CORE_PEER_MSPCONFIGPATH=$CORE_PEER_MSPCONFIGPATH" -e "CORE_PEER_ADDRESS=$CORE_PEER_ADDRESS" cli peer channel update -o orderer.example.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/$CORE_PEER_ANCHOR
  res=$? 
  set +x
  sleep $DELAY
  verifyResult $res "Failed to update the anchor peer  for ORG=$ORG and PEER=$PEER"
  echo 

}


install_chaincode_bidcc() {
 for ORG in ${ORGS[@]}; do
    for peer in 0; do		
        setEnvironment $peer $ORG
       #echo  $CORE_PEER_LOCALMSPID
       #echo $CORE_PEER_TLS_ROOTCERT_FILE
       #echo $CORE_PEER_MSPCONFIGPATH
       #echo $CORE_PEER_ADDRESS
       install_chiancode_bidcc_peer $peer $ORG 
       sleep $DELAY
     done    
   done
}


install_chiancode_bidcc_peer() {

PEER=$1
ORG="$2"
echo "=====================install chaincode bidcc ORG=$ORG and PEER=$CORE_PEER_ADDRESS  ===================== "
set -x
  docker exec -e "CORE_PEER_LOCALMSPID=$CORE_PEER_LOCALMSPID" -e "CORE_PEER_TLS_ROOTCERT_FILE=$CORE_PEER_TLS_ROOTCERT_FILE" -e "CORE_PEER_MSPCONFIGPATH=$CORE_PEER_MSPCONFIGPATH" -e "CORE_PEER_ADDRESS=$CORE_PEER_ADDRESS" cli peer chaincode install -n bidcc -v 1.0 -l java -p /opt/gopath/src/github.com/chaincode/bidcontractchaincode 	
  res=$? 
 set +x
 sleep $SMALLDELAY
 verifyResult $res "Failed to install chaincode PEER=$CORE_PEER_ADDRESS "
 echo 
}





instantiate_chaincode_bidcc_gov() {
 PEER=$1
 ORG="$2"
setEnvironment 0 "Contractor1MSP"
echo "=====================instantiate chaincode bidcc  ORG=$ORG and PEER=$CORE_PEER_ADDRESS  ===================== "
set -x
 docker exec -e "CORE_PEER_LOCALMSPID=$CORE_PEER_LOCALMSPID" -e "CORE_PEER_TLS_ROOTCERT_FILE=$CORE_PEER_TLS_ROOTCERT_FILE" -e "CORE_PEER_MSPCONFIGPATH=$CORE_PEER_MSPCONFIGPATH" -e "CORE_PEER_ADDRESS=$CORE_PEER_ADDRESS" cli peer chaincode instantiate -o orderer.example.com:7050 -C $CHANNEL_NAME -n bidcc -l java -v 1.0 -c '{"Args":["init","a","100","b","200"]}' -P "OR(AND ('GovMSP.peer','RegulatorMSP.peer'), AND ('GovMSP.peer','RegulatorMSP.peer' ,OR('Contractor1MSP.peer', 'Contractor2MSP.peer' )))" --collections-config "/opt/gopath/src/github.com/chaincode/collections_config.json"



  res=$? 
 set +x
 sleep $SMALLDELAY
 verifyResult $res "Failed to instantiate chaincode bidcc peer gov"
 echo 
}










start_bid_network
create_channel
add_peers_to_channel
update_anchor_peers
install_chaincode_bidcc
instantiate_chaincode_bidcc_gov








