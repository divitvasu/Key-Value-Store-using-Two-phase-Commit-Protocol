package com.distributedsystems.Server;

import com.distributedsystems.Coordinator.Forward_accept_Int;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;


//INTERFACE IMPLEMENTATIONS

class Store {                                                 // Data Store using Concurrent Hash-Map

    private ConcurrentHashMap<String, String> hmap;
    private static Store instance = new Store();

    private Store(){
        hmap = new ConcurrentHashMap<>();
    }

    public static Store getInstance() {
        return instance;
    }

    public void setMap(ConcurrentHashMap<String, String> val) {
        hmap = val;
    }

    public ConcurrentHashMap<String, String> getMap() {
        return hmap;
    }
}


class Accept_Client_Request_Int_Imp extends UnicastRemoteObject implements Accept_Client_Request_Int {
    private static Forward_accept_Int forward_obj;
    private static Phase1_Phase2_Int do_not_forward;

    public Accept_Client_Request_Int_Imp(Forward_accept_Int forward_obj, Phase1_Phase2_Int do_not_forward) throws RemoteException {
        super();
        this.forward_obj = forward_obj;
        this.do_not_forward = do_not_forward;
    }

    public ArrayList remote_send_request(ArrayList request) throws Exception{
        ArrayList response;                                   // If a get request is received then fetch from store and give
        String operation = (String) request.get(0);
        if (operation.equals("get")){
            response = do_not_forward.phase2(request);    // return result to client
            response.add("From connected Server");
        }
        else{                                         // for any other request forward the same to the coordinator
            response = forward_obj.remote_forward(request);   // return result from coordinator to client
            response.add("From Coordinator");
        }
        return response;
    }

}


class Key_Store_Int_Imp extends UnicastRemoteObject implements Key_Store_Int, Runnable {

    public static String IP_client = null;
    public static Logger lgm;           //static class variables for global access
    public static Handler fhl;
    private static Logger LOGGER = Logger.getLogger(String.valueOf(Server_app.class));

    static                                //static block for formatter output and logger
    {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS.%1$tL %1$Tp %2$s%n%4$s: %5$s%n");
        lgm = Logger.getLogger((Server_app.class.getClass().getName()));
    }

    public static void filing_logging() throws Exception{      //handling output of logger into a file

        fhl = new FileHandler("./Server_app.log");
        SimpleFormatter simple = new SimpleFormatter();
        fhl.setFormatter(simple);
        lgm.addHandler(fhl);

    }

    private ArrayList request_field;
    private Thread th;
    Store store_obj = Store.getInstance();
    public ArrayList res;

    public Key_Store_Int_Imp() throws Exception {
        super();
        filing_logging();
    }

    public void start() throws InterruptedException {     // Once a request is received from coordinator, start a new thread
//        lgm.log(Level.INFO, "Thread started...");
        if (th == null) {
            th = new Thread(this);
            th.start();
            th.join();
        }
    }

    public Key_Store_Int_Imp(ArrayList request_field) throws RemoteException {
        this.request_field = request_field;
    }

    public ArrayList Execute(ArrayList request) throws Exception {
        IP_client = getClientHost();                                // perform the required request
        Key_Store_Int_Imp serverTh = new Key_Store_Int_Imp(request);
        serverTh.start();

        return serverTh.setter();
    }

    @Override
    public void run() {         // check for validity and proceed accordingly

        try {
            String operation = (String) request_field.get(0);
            String key = (String) request_field.get(1);

            if ((operation.equalsIgnoreCase("put")) && (key != "") && ((String) request_field.get(2) != "")) {
                if (request_field.size() == 3)
                    res = put_pair(request_field);
            } else if ((operation.equalsIgnoreCase("get")) && (key != "")) {
                if (request_field.size() == 2)
                    res = get_pair(request_field);
            } else if ((operation.equalsIgnoreCase("del")) && (key != "")) {
                if (request_field.size() == 2)
                    res = delete_pair(request_field);
            } else {
                //log into logger and send the status back to the client
                res = new ArrayList();
                lgm.log(Level.SEVERE, "ERR- Datagram " + request_field + " received from client "+ IP_client +" is invalid!");
                res.add("ERR- Datagram " + request_field + " is invalid!");
                System.out.println("ERR- Datagram " + request_field + " is invalid!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList put_pair(ArrayList messages){      // perform put-operation and return the response

        ConcurrentHashMap<String, String> retrieved = store_obj.getMap();  //class store
        ArrayList<String> response = new ArrayList<>();

        if (!retrieved.containsKey((String) messages.get(1))) {
            retrieved.put((String) messages.get(1), (String) messages.get(2));
            lgm.log(Level.INFO,"Request "+messages+" from client "+IP_client+" performed on Hashmap");

            store_obj.setMap(retrieved);
            response.add("ACK-Added pair! "+ messages.get(1)+":"+messages.get(2));
            response.add("Data-Store contents: "+store_obj.getMap());
            System.out.println("ACK-Added pair!");
            System.out.println(store_obj.getMap());
        }
        else{                                    //send response to the client, else return and log an error
            System.out.println("ERR-Could not add Key, already exists!");
            response.add("ERR-Could not add Key, already exists!");
            lgm.log(Level.WARNING,"Key "+messages.get(1)+ " already exists");
        }
        return response;
    }

    public synchronized ArrayList get_pair(ArrayList messages) {  // perform get-operation and return the response

        ConcurrentHashMap<String, String> retrieved = store_obj.getMap();
        ArrayList<String> response = new ArrayList<>();

        String k = (String) messages.get(1);
        String v;

        if (retrieved.containsKey(k)){
            v = (String) retrieved.get(k);

            response.add("ACK-Fetched pair! " + k + ":" + v);
            response.add("Data-Store contents: "+store_obj.getMap());
            System.out.println("ACK-Fetched pair! " + k + ":" + v);
            lgm.log(Level.INFO,"Request "+messages+" from client "+IP_client+" performed on Hashmap");

        }                                       //send to the client, else return and log an error
        else{
            System.out.println("ERR-Could not find Key to get, DNE!");
            response.add("ERR-Could not find Key to get, DNE!");
            lgm.log(Level.WARNING,"Key: "+messages.get(1)+ " DNE in Hashmap!");
        }
        return response;
    }

    public synchronized ArrayList delete_pair(ArrayList messages) {    // perform del-operation and return the response

        ConcurrentHashMap<String, String> retrieved = store_obj.getMap();
        ArrayList<String> response = new ArrayList<>();

        if (retrieved.containsKey((String) messages.get(1))){
            retrieved.remove((String) messages.get(1));

            store_obj.setMap(retrieved);
            response.add("ACK-Deleted pair with key! "+messages.get(1));
            response.add("Data-Store contents: "+store_obj.getMap());
            System.out.println("ACK-Deleted pair! ");
            System.out.println(store_obj.getMap());
            lgm.log(Level.INFO,"Request "+messages+" from client "+IP_client+" performed on Hashmap");

        }
        else{                                       //send response to the client, else return and log an error
            System.out.println("ERR-Could not find Key to delete, DNE!");
            response.add("ERR-Could not find Key to delete, DNE!");
            lgm.log(Level.WARNING,"Key: "+messages.get(1)+ " to be deleted DNE in Hashmap");
        }
        return response;
    }

    public synchronized ArrayList setter(){                       //for accessing the result from the main() function
        return this.res;
    }     // return from thread
}


class Phase1_Phase2_Int_Imp extends UnicastRemoteObject implements Phase1_Phase2_Int{
    int port;                                                 // class to implement phase1 and phase2 of the transaction

    public Phase1_Phase2_Int_Imp(int port_listen) throws Exception {
        super();
        this.port = port_listen;
    }

    public String phase1(String str) throws Exception { // called by coordinator
        String reply = null;
        if (str.equals("Prepare")){
            reply = "OK";
        }

        return reply;
    }
    public ArrayList phase2(ArrayList request) throws Exception {      // called by coordinator if everything is OK

        Registry registry = LocateRegistry.getRegistry("127.0.0.1", port);    // bind to the registry provided by the
        Key_Store_Int commit_data = (Key_Store_Int) registry.lookup("Key_Store_Int_Imp");   //server

        ArrayList resp = commit_data.Execute(request);    // perform the operation on the key-value store
        return resp;
    }

}

//SERVER APP BEGINS HERE

public class Server_app {

    public static Logger lgm;           //static class variables for global access
    public static Handler fhl;
    public static int port_listen;
    public static int coord_port;
    public static String coord_address;

    private static Logger LOGGER = Logger.getLogger(String.valueOf(Server_app.class));

    static                                //static block for formatter output and logger
    {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS.%1$tL %1$Tp %2$s%n%4$s: %5$s%n");
        lgm = Logger.getLogger((Server_app.class.getClass().getName()));
    }

    public static void filing_logging() throws Exception{      //handling output of logger into a file

        fhl = new FileHandler("./Server_app.log");
        SimpleFormatter simple = new SimpleFormatter();
        fhl.setFormatter(simple);
        lgm.addHandler(fhl);
    }

    public static void main(String[] args) throws Exception {

        filing_logging();

        if (args.length == 3){                         //accept command line arguments, if none provided take defaults
            port_listen = Integer.parseInt(args[0]);
            coord_address = args[1];
            coord_port = Integer.parseInt(args[2]);
        }
        else{
            port_listen = 7051;
            coord_address = "127.0.0.1";
            coord_port = 7050;
        }

        try{

            Registry coord_registry = LocateRegistry.getRegistry(coord_address, coord_port);    // bind to the registry provided by the
            Forward_accept_Int send_data = (Forward_accept_Int) coord_registry.lookup("Forward_Accept");   //coordinator

            Registry registry = LocateRegistry.createRegistry(port_listen);  // create the registries for this instance

            Phase1_Phase2_Int query = new Phase1_Phase2_Int_Imp(port_listen);
            registry.bind("Phase1_Phase2", query);
            lgm.log(Level.INFO, "Ph1_Ph2 OBJ BINDING SUCCESSFUL");

            Accept_Client_Request_Int accept = new Accept_Client_Request_Int_Imp(send_data, query);
            registry.bind("Accept_Client", accept);
            lgm.log(Level.INFO, "FORWARDING OBJ BINDING SUCCESSFUL");

            Key_Store_Int msObj = new Key_Store_Int_Imp();
            registry.bind("Key_Store_Int_Imp", msObj);
            lgm.log(Level.INFO, "STORE OBJ BINDING SUCCESSFUL");

        }
        catch(Exception e){
            lgm.log(Level.SEVERE, "UNABLE TO BIND!");
            System.out.println(e);
        }
    }
}