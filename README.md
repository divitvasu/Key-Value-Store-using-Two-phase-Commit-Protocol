# Key-Value-Store-using-Two-phase-Commit-Protocol (2021)

> Tools - Java, Docker, Shell Scripting

Key-Value-Store based on a Client-Server architecture, using a 2 Phase-Commit-Protocol to guarantee consistency across different servers. The server applications are multithreaded for added concurrency and can handle multiple clients at once.

## About

- Upon execution, the pre-defined operations are performed and then the user is asked for the input of whatever it wants from the list of choices. The user has the option to perform three operations on the server. The user input is received at the server. If it is a ‘get’ request, the needed key-value pair is fetched from the store and returned to the client. However, if the received request is for a ‘put’ or a ‘delete’ operation, then the request is forwarded to the coordinator. The coordinator upon receiving the forwarded request, initiates the two-phase-commit-protocol. The phase-1 of the protocol checks for the readiness of all the servers. If all the servers are ready, then in the next phase, a commit operation is performed to all the instances of the server. This ensures that the data is consistent across all the server nodes. At the individual server the input message is checked for consistency and correctness. If the message is invalid, a log is made, and the client moves on to providing the next input. If the message is valid, the respective operation is performed on the key-value store as directed by the user input. The acknowledgement of the same is sent back to the client, and the client logs the same on its side. If the transaction fails for some reason, then the respective response is also sent back to the client via the server, the client is connected to.
- The remote methods are made available between all the participants using Java RMI. When the coordinator is executed, it waits for all the servers to bind their objects to a registry for it to access. When all the servers are up, they connect to the coordinator’s registry too and in turn, publish a registry for the client to access. The client uses this object to send the request packet to the server. The server then decides what needs to be done with the packet. Whenever the coordinator invokes the phase2 method, a corresponding thread is executed on each server, which in turns calls a synchronized method to facilitate the required operation. A separate class, which serves as a common store for each instance defines a concurrent hashmap.
- All three applications define a logger to store the logs into separate files for the servers, client, and the coordinator.


## Project Structure

Under the ‘Server’ folder in the package, the following files exist:
- *Server_app.java* This file contains the java code for the server application and implements the below mentioned interfaces. This file takes in three command-line arguments in the following format: `java com.distributedsystems.Server.Server_app <server_listening_port> <coordinator_IP_address> <coordinator_listening_port>`

- *Key_Store_Int.java* Contains the java code for the definition of the interface for the Key-value store. This file supplements the Server java file.

- *Phase1_Phase2_Int.java* Contains the java code for the implementation of the interface for the two phase-commit-protocol. The coordinator uses the remote methods of this interface to access the servers for needed operations. This file supplements the Server java file.

- *Accept_Client_Request_Int.java* Contains the java code for the implementation of the interface which defines the remote methods for access by the client. The client uses this to send the user requests to the server.

Under the ‘Coordinator’ folder in the package, the following files exist:
- *Coordinator.java* Contains the java code for the coordinator application and implements the below mentioned interfaces. This file accepts eleven command-line arguments that comprise of its own listening port and a combination of the server ports and addresses it needs to connect to. This file takes in eleven command-line arguments in the following format: `java com.distributedsystems.Coordinator.Coordinator <listen_port> <server1_listen_port><server2_listen_port><server3_listen_port> <server4_listen_port><server5_listen_port><server1_address> <server2_address><server3_address><server4_address> <server5_address>`

- *Forward_accept_Int.java* Contains the java code for the interface definition, that enables the coordinator to accept the forwarded requests from any of the servers. This interface publishes a remote method to all the instances of the server application and sends the response back to the server of the execution. This file supports the coordinator file.

At the root of the package exists:
- *Client1_app.java* This file contains the java code for the client application. This application takes input from the user for the operations to be performed on the key-value store. This file is preloaded with 5 put, get, and delete operations. The client application can connect to any of the five servers as desired. This file takes in two command-line arguments in the following format: `java com.distributedsystems.Client1_app <server_IP_addresss> <server_listening_port>`

The main directory contains the following files necessary for execution of the project on Docker:
- *Dockerfile* This contains the parameters to generate and create images of the builds for the servers, coordinator and the client. JDK 17 from the alpine image has been used.

- *coordinator.sh* This file contains all the instructions for building images for each, the coordinator, the client and the servers. Five separate containers are prepared for a single server image. This file cleans up any existing resources, and starts afresh, building new images for all three applications. It targets the details for each build from the Dockerfile. Note that the default 'host' network has been used to avoid exposing any ports, as, on a custom network, the required ports need to be exposed manually. This has been done with the aim to reduce any complexities during the execution. Lastly, this script brings up the coordinator container. This file needs to be executed first and takes in eleven command-line arguments in the following format: `./coordinator.sh <listen_port> <server1_listen_port><server2_listen_port><server3_listen_port> <server4_listen_port><server5_listen_port><server1_address> <server2_address><server3_address><server4_address> <server5_address>`

- *server{1-5}.sh* These files together contain the instructions for starting up individual servers as separate containers. Note that the default 'host' network has been used to avoid exposing any ports, as, on a custom network, the required ports need to be exposed manually. This has been done with the aim to reduce any complexities during the execution. These 5 files need to be run once the coordinator is up and running. The files take in three command-line argument, each in the following format: `./server{1-5}.sh <server_listening_port> <coordinator_IP_address> <coordinator_listening_port>`

- *client.sh* This file contains all the configurations needed to start the client container that has been created in the previous step. The client container also gets attached to the default 'host' network. This file needs to be executed once the five servers and the coordinator are up and running. This file takes in two command-line arguments in the following format: `./client.sh <server_address> <server_listening_port>`

## Sample Execution

The commands are to be executed in order and on separate Linux shells, so in all 7 terminals windows will be required. The respective logs fall inside the docker images and note, that the scripts will need the command line arguments to run. However, if executing on barebones CMD prompt, the command line arguments are optional and the applications will fall-back to the default ports, if no arguments are provided. The execution for the project is as follows.

- Executing the application using docker and Linux Shell:
  - `./coordinator.sh 7050 7051 7052 7053 7054 7055 localhost localhost localhost localhost localhost`
  - `./server1.sh 7051 localhost 7050`
  - `./server2.sh 7052 localhost 7050`
  - `./server3.sh 7053 localhost 7050`
  - `./server4.sh 7054 localhost 7050`
  - `./server5.sh 7055 localhost 7050`
  - `./client.sh localhost 7051`
  - The respective logs fall inside the docker images.

**Coordinator script output**
<p align="center">
<img src="https://github.com/divitvasu/Key-Value-Store-using-Two-phase-Commit-Protocol/assets/30820920/87204708-13da-4fe7-ad4a-583e2f42a5c3" alt="Image" width="650" height="500">
</p>

**Server script output (only one shown out of five)**
<p align="center">
<img src="https://github.com/divitvasu/Key-Value-Store-using-Two-phase-Commit-Protocol/assets/30820920/f9702660-7c23-4813-a1cc-48c0d15b5cd0" alt="Image" width="650" height="500">
</p>

**Client script output**
<p align="center">
<img src="https://github.com/divitvasu/Key-Value-Store-using-Two-phase-Commit-Protocol/assets/30820920/32ad83e9-5b9b-4537-a649-35086519e3bc" alt="Image" width="650" height="500">
</p>

## Concluding Thoughts

The server provides with three multi-threaded methods and one remote method for the client to be able to send its request. The coordinator also publishes a remote method for the server to be able to forward the client requests. If the client request is for a get operation, the server fetches the result from its own store and sends back to the client. The request for a get operation, need not be forwarded to the coordinator as all replicas are consistent. If the request is for a put or a delete operation, the same is forwarded to the coordinator and the coordinator then initiates the two phase-commit-protocol. This is done using the remote methods that have been published by each of the servers, namely the remote methods phase1() and phase2(). During the phase1 call, the coordinator waits for an ‘OK’ message from each of the server replicas. If all ‘OK’ messages are received, then at that particular instant none of the servers can flake off, and the coordinator immediately proceeds with calling the phase2() on each replica, which is essentially a commit operation. This ensures that the request reaches all the servers at the same instant and the updates performed remain consistent. Just in case, one of the server replicas is made to fail, the coordinator does not receive an ‘OK’ message from the replica. The coordinator logs the replica number in it’s logs and aborts the forthcoming transactions. This implementation does handle the failure of the nodes and when the failed replica is brought up again, transactions do get performed again, without the need for terminating the program and starting afresh. However, from this point onwards the replica that was brought up, does not remain consistent with the other nodes. This is because, when the replica is brought up again, it has a freshly initialised object for its key-value store. 

To extend this implementation, whenever, a replica is brought up again, it should ideally start with initialising it’s store to the values stored in the other replicas. One such way, to achieve this would be to implement an algorithm to make the newly started node to contact the other nodes on the network and arrive at a common consensus for the values in the key-value store. This could be done using timestamps. The other way would be that the newly started node polls the coordinator and the coordinator in turn, would poll any one of the servers and provide the newly started node with the most recent contents of the key-value store. One drawback of the two phase-commit-protocol is that it relies extensively on a central coordinator node, and if the coordinator fails, the entire distributed network risks getting inconsistent. So, necessary measures need to be implemented to account for the failure of the coordinator node. Also, there are chances that the coordinator fails immediately after calling phase1() itself and the entire transaction never goes through. Also, one improvement to the implementation would be to check for the data packets validity on the client node itself. This would save a lot of bouncing requests, in case the data packet entered by the user is invalid. The project moreover uses a concurrent hashmap in lieu of the simple implementation, for added concurrency, whenever possible. A key assumption here is that the number of clients is only one, and no server nodes fail. For multiple clients some modifications would be needed. Also, the above implementation would terminate if the server node that the client is immediately connected to, fails. However, the failure with other replicas gets handled. The replica that the client is desired to be connected to can be provided to the client as command line arguments when starting it up. The code is fixed for working with five replicas, however, this can very well be made to be dynamic, by asking the user for the number of replicas to work with.

## References

- [JAVA RMI](https://www.javatpoint.com/RMI)
- [Multi-threading in Java](https://www.geeksforgeeks.org/multithreading-in-java/)
- [Two Phase Commit Protocol](https://en.wikipedia.org/wiki/Two-phase_commit_protocol)
- Distributed Systems – Concepts and Design – George Coulouris, Jean Dollimore, Tim Kindberg, Gordon Blair
