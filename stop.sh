IMAGETAG="latest"

stopbidnetwork() {
 docker-compose -f docker-compose-cli.yaml down --volumes --remove-orphans
 
 echo "===================== bidnetwork stoped ===================== "
}

removeContainers(){

 #Delete any ledger backups
   docker run -v $PWD:/tmp/first-network --rm hyperledger/fabric-tools:$IMAGETAG rm -Rf /tmp/first-network/ledgers-backup
    #Cleanup the chaincode containers
    clearContainers
    #Cleanup images
    removeUnwantedImages




}

function clearContainers() {
  CONTAINER_IDS=$(docker ps -a | awk '($2 ~ /dev-peer.*.allocatecc.*/) {print $1}')
  if [ -z "$CONTAINER_IDS" -o "$CONTAINER_IDS" == " " ]; then
    echo "---- No containers available for deletion allocatecc ----"
  else
    docker rm -f $CONTAINER_IDS
  fi


 CONTAINER_IDS=$(docker ps -a | awk '($2 ~ /dev-peer.*.bidcc.*/) {print $1}')
  if [ -z "$CONTAINER_IDS" -o "$CONTAINER_IDS" == " " ]; then
    echo "---- No containers available for deletion bidcc ----"
  else
    docker rm -f $CONTAINER_IDS
  fi
}

# Delete any images that were generated as a part of this setup
# specifically the following images are often left behind:
# TODO list generated image naming patterns
function removeUnwantedImages() {

 	
  DOCKER_IMAGE_IDS=$(docker images | awk '($1 ~ /dev-peer.*.allocatecc.*/) {print $3}')
  if [ -z "$DOCKER_IMAGE_IDS" -o "$DOCKER_IMAGE_IDS" == " " ]; then
    echo "---- No images available for deletion allocatecc----"
  else
    docker rmi -f $DOCKER_IMAGE_IDS
  fi

  DOCKER_IMAGE_IDS=$(docker images | awk '($1 ~ /dev-peer.*.bidcc.*/) {print $3}')
  if [ -z "$DOCKER_IMAGE_IDS" -o "$DOCKER_IMAGE_IDS" == " " ]; then
    echo "---- No images available for deletion bidcc ----"
  else
    docker rmi -f $DOCKER_IMAGE_IDS
  fi

}


stopbidnetwork
removeContainers
