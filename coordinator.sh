PROJECT_NETWORK='host'
SERVER_IMAGE='server_app'
SERVER1_CONTAINER='server-1'
SERVER2_CONTAINER='server-2'
SERVER3_CONTAINER='server-3'
SERVER4_CONTAINER='server-4'
SERVER5_CONTAINER='server-5'

CLIENT_IMAGE='client'
CLIENT_CONTAINER='client-1'

COORD_IMAGE='coordinator'
COORD_CONTAINER='coordinator-1'



# clean up existing resources, if any
echo "----------Cleaning up existing resources----------"
docker container stop $SERVER1_CONTAINER 2> /dev/null && docker container rm $SERVER1_CONTAINER 2> /dev/null
docker container stop $SERVER2_CONTAINER 2> /dev/null && docker container rm $SERVER2_CONTAINER 2> /dev/null
docker container stop $SERVER3_CONTAINER 2> /dev/null && docker container rm $SERVER3_CONTAINER 2> /dev/null
docker container stop $SERVER4_CONTAINER 2> /dev/null && docker container rm $SERVER4_CONTAINER 2> /dev/null
docker container stop $SERVER5_CONTAINER 2> /dev/null && docker container rm $SERVER5_CONTAINER 2> /dev/null

docker container stop $CLIENT_CONTAINER 2> /dev/null && docker container rm $CLIENT_CONTAINER 2> /dev/null

docker container stop $COORD_CONTAINER 2> /dev/null && docker container rm $COORD_CONTAINER 2> /dev/null
#docker network rm $PROJECT_NETWORK 2> /dev/null

# only cleanup
if [ "$1" == "cleanup-only" ]
then
  exit
fi

# create a custom virtual network
#echo "----------creating a virtual network----------"
#docker network create $PROJECT_NETWORK

# build the images from Dockerfile
echo "----------Building images----------"
docker build -t $COORD_IMAGE --target coordinator-build .
docker build -t $SERVER_IMAGE --target server-build .
docker build -t $CLIENT_IMAGE --target client-build .

# run the image and open the required ports
echo "----------Running Coordinator app----------"
docker run --rm --name $COORD_CONTAINER --network $PROJECT_NETWORK $COORD_IMAGE java com.distributedsystems.Coordinator.Coordinator "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9" "$10" "$11"
