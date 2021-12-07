package com.distributedsystems.Coordinator;

import com.distributedsystems.Server.Phase1_Phase2_Int;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;


class Forward_accept_Int_Imp extends UnicastRemoteObject implements Forward_accept_Int {

    public ArrayList temp;
    ArrayList response_to_caller;
    ArrayList helper;

    public static Logger lgm;           //static class variables for global access
    public static Handler fhl;
    public static Logger LOGGER = Logger.getLogger(String.valueOf(Coordinator.class));

    static                                //static block for formatter output and logger
    {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS.%1$tL %1$Tp %2$s%n%4$s: %5$s%n");
        lgm = Logger.getLogger((Coordinator.class.getClass().getName()));
    }

    public static void filing_logging() throws Exception{      //handling output of logger into a file

        fhl = new FileHandler("./Coordinator.log");
        SimpleFormatter simple = new SimpleFormatter();
        fhl.setFormatter(simple);
        lgm.addHandler(fhl);

    }

    public Forward_accept_Int_Imp() throws Exception {
        super();
        filing_logging();
    }

    public ArrayList remote_forward(ArrayList request) throws Exception{
        temp = request;                                 // accept the forwarded request from the server
        while (helper == null){
            helper = response_to_caller;               // wait for the main() function to set the response
        }                                              // to be returned to the client
        helper = null;

        return this.response_to_caller;
    }

    public ArrayList getter() throws Exception{
        return this.temp;
    }

    public void setter() throws Exception {
        this.temp = null;
        this.response_to_caller = null;
    }

    public void setter_response(ArrayList resp) throws Exception {
        this.response_to_caller = resp;
    }
}

public class Coordinator extends Forward_accept_Int_Imp{

    public static int port_listen;               // main coordinator class
    public static int server1_port;              // all server connection details
    public static int server2_port;
    public static int server3_port;
    public static int server4_port;
    public static int server5_port;
    public static String server1_addr;
    public static String server2_addr;
    public static String server3_addr;
    public static String server4_addr;
    public static String server5_addr;

    public static int count_servers = 0;          // registry objects
    public static Phase1_Phase2_Int com_ch1, com_ch2, com_ch3, com_ch4, com_ch5;
    public static Registry registry1, registry2, registry3, registry4, registry5;

    public Coordinator() throws Exception {
    }

    public static void refresh_registry() throws Exception{ // function to connect to all server's remote objects

        registry1 = LocateRegistry.getRegistry("127.0.0.1", server1_port);    // bind to the registry provided by the
        com_ch1 = (Phase1_Phase2_Int) registry1.lookup("Phase1_Phase2");   //server

        registry2 = LocateRegistry.getRegistry("127.0.0.1", server2_port);    // bind to the registry provided by the
        com_ch2 = (Phase1_Phase2_Int) registry2.lookup("Phase1_Phase2");   //server

        registry3 = LocateRegistry.getRegistry("127.0.0.1", server3_port);    // bind to the registry provided by the
        com_ch3 = (Phase1_Phase2_Int) registry3.lookup("Phase1_Phase2");   //server

        registry4 = LocateRegistry.getRegistry("127.0.0.1", server4_port);    // bind to the registry provided by the
        com_ch4 = (Phase1_Phase2_Int) registry4.lookup("Phase1_Phase2");   //server

        registry5 = LocateRegistry.getRegistry("127.0.0.1", server5_port);    // bind to the registry provided by the
        com_ch5 = (Phase1_Phase2_Int) registry5.lookup("Phase1_Phase2");   //server
    }

    public static void main(String[] args) throws Exception{

        if (args.length == 11) {                   //accept command line arguments, if none provided take defaults
            port_listen = Integer.parseInt(args[0]);
            server1_port = Integer.parseInt(args[1]);
            server2_port = Integer.parseInt(args[2]);
            server3_port = Integer.parseInt(args[3]);
            server4_port = Integer.parseInt(args[4]);
            server5_port = Integer.parseInt(args[5]);

            server1_addr = args[6];
            server2_addr = args[7];
            server3_addr = args[8];
            server4_addr = args[9];
            server5_addr = args[10];

        }

        else{
            port_listen = 7050;
            server1_port = 7051;
            server2_port = 7052;
            server3_port = 7053;
            server4_port = 7054;
            server5_port = 7055;

            server1_addr = "127.0.0.1";
            server2_addr = "127.0.0.1";
            server3_addr = "127.0.0.1";
            server4_addr = "127.0.0.1";
            server5_addr = "127.0.0.1";
        }

        try{

            Forward_accept_Int accept = new Forward_accept_Int_Imp();    // create registry for all servers to access
            Registry registry_coordinator = LocateRegistry.createRegistry(port_listen);  //all forwarded requests come here
            registry_coordinator.bind("Forward_Accept", accept);                   // through this interfaces remote method
            lgm.log(Level.INFO, "OBJECT TO ACCEPT REQUESTS BOUND SUCCESSFULLY");

            System.out.println("Waiting for servers to expose remote methods...");
            TimeUnit.SECONDS.sleep(10);                       // wait for all servers to come up
//            refresh_registry();
            System.out.println("Waiting for requests...");

            count_servers = 5;

            while (true){
                boolean flag = true;
                ArrayList data = new ArrayList();
                ArrayList status = new ArrayList();
                ArrayList response = new ArrayList();

                while (((Forward_accept_Int_Imp) accept).getter() == null){  // wait until client sends a request to the server
                    TimeUnit.SECONDS.sleep(1);                    // and the server forwards the same to the coordinator
                    data = ((Forward_accept_Int_Imp) accept).getter();
                }
                System.out.println("Data packet forwarded: "+data);        // proceed once a datagram is received
                lgm.log(Level.INFO, "FORWARDED PACKET: "+data);
                try{
                    refresh_registry();                  // if any of the registry is not found, there is a potential failure
                }
                catch(Exception e){
                    System.out.println("One or more replicas are down!");
                    lgm.log(Level.SEVERE, "ONE OR MORE REPLICAS ARE DOWN");
                }
                try{                                               //check registry 1 for availability, if not available
                    registry1.lookup("Phase1_Phase2");       // mark failed, similarly for all other registries of 5 replicas
                    status.add(com_ch1.phase1("Prepare"));
                }
                catch (Exception e){
                    System.out.println("Replica-1 not responding!");
                    lgm.log(Level.SEVERE, "REPLICA-1 DOWN");
                    status.add("Failed");
                }

                try{                                               //check registry 2
                    registry2.lookup("Phase1_Phase2");
                    status.add(com_ch2.phase1("Prepare"));
                }
                catch (Exception e){
                    System.out.println("Replica-2 not responding!");
                    lgm.log(Level.SEVERE, "REPLICA-2 DOWN");
                    status.add("Failed");
                }

                try{                                               //check registry 3
                    registry3.lookup("Phase1_Phase2");
                    status.add(com_ch3.phase1("Prepare"));
                }
                catch (Exception e){
                    System.out.println("Replica-3 not responding!");
                    lgm.log(Level.SEVERE, "REPLICA-3 DOWN");
                    status.add("Failed");
                }

                try{                                               //check registry 4
                    registry4.lookup("Phase1_Phase2");
                    status.add(com_ch4.phase1("Prepare"));
                }
                catch (Exception e){
                    System.out.println("Replica-4 not responding!");
                    lgm.log(Level.SEVERE, "REPLICA-4 DOWN");
                    status.add("Failed");
                }

                try{                                               //check registry 5
                    registry5.lookup("Phase1_Phase2");
                    status.add(com_ch5.phase1("Prepare"));
                }
                catch (Exception e){
                    System.out.println("Replica-5 not responding!");
                    lgm.log(Level.SEVERE, "REPLICA-5 DOWN");
                    status.add("Failed");
                }

                System.out.println(status);                 // check the status from all 5 replicas

                for (int i = 0; i <= count_servers-1; i++) {
                    if (!status.get(i).equals("OK")) {    // if any of the 5 replicas have failed we need to abort the following steps
                        System.out.println("Aborting this Transaction!");    // this is facilitated using a flag
                        lgm.log(Level.SEVERE, "CURRENT TRANSACTION ABORTED");
                        flag = false;
                        break;
                    }
                }

                if (flag == true){                    // if all replicas have confirmed, they cannot back-off now
                    response = com_ch1.phase2(data);  // we call phase-2 on all replicas to perform the necessary commit on all
                    response = com_ch2.phase2(data);  // stores all at once, so as to, maintain consistency
                    response = com_ch3.phase2(data);
                    response = com_ch4.phase2(data);
                    response = com_ch5.phase2(data);

                    lgm.log(Level.INFO, "COMMIT/PHASE2 SUCCESSFUL ON ALL REPLICAS");
                    ((Forward_accept_Int_Imp) accept).setter_response(response);   // send needed response back to the called
                    TimeUnit.SECONDS.sleep(1);                              // method in the above class
                    ((Forward_accept_Int_Imp) accept).setter();                    // re-initialise response for new request
                }                                                                  // purging the details of the current packet
                else{
                    lgm.log(Level.SEVERE, "PHASE1 FAILED");
                    response.add("Transaction unsuccessful, One or more servers did not respond to phase1!");
                    ((Forward_accept_Int_Imp) accept).setter_response(response);
                    TimeUnit.SECONDS.sleep(1);
                    ((Forward_accept_Int_Imp) accept).setter();
                }
            }
        }
        catch(Exception e){                                          // if an exception cannot be dealt with, program terminates
            System.out.println("UNABLE TO BIND!");
            lgm.log(Level.SEVERE, "UNABLE TO BIND!");
            System.out.println(e);
        }
    }
}
