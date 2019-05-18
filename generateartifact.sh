CHANNEL_NAME=bidchannel

FABRIC_CFG_PATH=${PWD}

generatecryptomaterial() {
echo
  echo "##########################################################"
  echo "##### Generate certificates using cryptogen tool #########"
  echo "##########################################################"

  if [ -d "crypto-config" ]; then
    rm -Rf crypto-config
    rm -Rf channel-artifacts
    rm channel-artifacts
  fi
  set -x
  ./bin/cryptogen generate --config=./crypto-config.yaml
  res=$?
  set +x
  if [ $res -ne 0 ]; then
    echo "Failed to generate certificates..."
    exit 1
  fi
  echo
 
}

createartificatdirectory() {
mkdir channel-artifacts
}

generategenesisblock() {
 echo "##### Generate genesis block using configtxgen tool #########" 
 #./bin/configtxgen -profile BiddingGenesis -channelID bidn-sys-channel -outputBlock ./channel-artifacts/genesis.block
  ./bin/configtxgen -profile BidOrdererGenesis -channelID bidn-sys-channel -outputBlock ./channel-artifacts/genesis.block

 echo "##### genesis block generated using configtxgen tool #########"
}


generatechannel() {
 echo "##### Generate channel block using configtxgen tool $CHANNEL_NAME #########" 
 ./bin/configtxgen -profile BidChannel -outputCreateChannelTx ./channel-artifacts/channel.tx -channelID $CHANNEL_NAME
 echo "##### channel block generated using configtxgen tool #########"
}

generateanchorpeers() {
 
 echo "##### Generate Anchor peers using configtxgen tool #########"
 
 ./bin/configtxgen -profile BidChannel -outputAnchorPeersUpdate ./channel-artifacts/GovMSPanchors.tx -channelID $CHANNEL_NAME -asOrg GovMSP 
 ./bin/configtxgen -profile BidChannel -outputAnchorPeersUpdate ./channel-artifacts/RegulatorMSPanchors.tx -channelID $CHANNEL_NAME -asOrg RegulatorMSP
 ./bin/configtxgen -profile BidChannel -outputAnchorPeersUpdate ./channel-artifacts/Contractor1MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Contractor1MSP
 ./bin/configtxgen -profile BidChannel -outputAnchorPeersUpdate ./channel-artifacts/Contractor2MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Contractor2MSP
 echo "##### Anchor peers generated using configtxgen tool #########" 

}


generatecryptomaterial
createartificatdirectory
generategenesisblock
generatechannel
generateanchorpeers	


