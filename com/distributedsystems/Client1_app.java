package com.distributedsystems;

import com.distributedsystems.Server.Accept_Client_Request_Int;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class Client1_app {

    private static Logger lgm;           //static class variables for global access
    public static Handler fhl;
    public static int server_port;
    public static String server_addr;

    static                                //static block for formatter output and logger
    {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS.%1$tL %1$Tp %2$s%n%4$s: %5$s%n");
        lgm = Logger.getLogger((Client1_app.class.getClass().getName()));
    }

    private static void filing_logging() throws Exception{      //handling output of logger into a file

        fhl = new FileHandler("./Clientlogs.log");
        SimpleFormatter simple = new SimpleFormatter();
        fhl.setFormatter(simple);
        lgm.addHandler(fhl);
    }

    public static void main(String args[]) throws Exception {

        filing_logging();                         //start logging

        if (args.length == 2){                            //accept command line arguments, if none provided take defaults
            server_addr = args[0];                        //client can connect to any server instance of wish
            server_port = Integer.parseInt(args[1]);
        }
        else{
            server_addr = "127.0.0.1";
            server_port = 7051;
        }

        Registry registry = LocateRegistry.getRegistry(server_addr, server_port);    // bind to the registry provided by the
        Accept_Client_Request_Int send_data = (Accept_Client_Request_Int) registry.lookup("Accept_Client");   //server

        lgm.log(Level.INFO,"BINDING SUCCESSFUL WITH THE REMOTE METHODS");

        System.out.println("Performing pre-defined 5 Puts, 5 Gets, 5 Deletes before handing over to the user...");

//        TimeUnit.SECONDS.sleep(1);
        for (int i = 1; i <= 5; i++) {                         //5 pre-defined operations as required by the question

            String k = String.valueOf(i);
            char v = (char) ((char) i + 64);
            put_pair(k, String.valueOf(v), send_data);
            TimeUnit.SECONDS.sleep(1);
        }

//        TimeUnit.SECONDS.sleep(1);
        for (int i = 1; i <= 5; i++) {

            String k = String.valueOf(i);
            get_pair(k, send_data);
            TimeUnit.SECONDS.sleep(1);
        }

//        TimeUnit.SECONDS.sleep(1);
        for (int i = 1; i <= 5; i++) {

            String k = String.valueOf(i);
            delete_pair(k, send_data);
            TimeUnit.SECONDS.sleep(1);
        }

        while(true){                                //user-input is accepted here-on

            TimeUnit.SECONDS.sleep(1);
            Scanner console = new Scanner(System.in);
            System.out.println("");
            System.out.println("Enter your choice...");
            System.out.println("1). Send a PUT request.");
            System.out.println("2). Send a GET request.");
            System.out.println("3). Send a DELETE request.");
            System.out.println("4). End.");
            int choice = console.nextInt();

            Scanner sec = new Scanner(System.in);
            switch (choice) {
                case 1:

                    System.out.println("Enter Key to send");
                    String key_put = sec.nextLine();
                    System.out.println("Enter Value to send");
                    String val_put = sec.nextLine();
                    put_pair(key_put, val_put, send_data);     //insert the pair
                    break;

                case 2:
                    System.out.println("Enter Key to fetch");
                    String key_get = sec.nextLine();
                    get_pair(key_get, send_data);             //fetch the pair
                    break;

                case 3:
                    System.out.println("Enter Key to delete");
                    String key_del = sec.nextLine();
                    delete_pair(key_del, send_data);           //delete the pair
                    break;

                case 4:
                    System.exit(0);                   //if user wants to terminate client

                default:
                    System.out.println("Invalid choice!!");
                    break;
            }
        }
    }

    public static void put_pair(String key, String val, Accept_Client_Request_Int obj) throws Exception{

        ArrayList response = new ArrayList();
        ArrayList<String> requests = new ArrayList<>();             //function to send the key-value pair to the server
        requests.add("put");
        requests.add(key);
        requests.add(val);
        System.out.println("Sending Put req" + requests);
        lgm.log(Level.INFO,"PUT-request sent to the server "+ requests);

        response = obj.remote_send_request(requests);       //log necessary information in the logger and accept response

        System.out.println("Server message: "+ response);
        lgm.log(Level.INFO,"Server response "+ response);
    }

    public static void get_pair(String key, Accept_Client_Request_Int obj) throws Exception{

        ArrayList response = new ArrayList();
        ArrayList<String> requests = new ArrayList<>();         //function to fetch the key-value pair from the server
        requests.add("get");
        requests.add(key);
        System.out.println("Sending Get req" + requests);
        lgm.log(Level.INFO,"GET-request sent to the server "+ requests);

        response = obj.remote_send_request(requests);      //log necessary information in the logger and accept response

        System.out.println("Server message: "+ response);
        lgm.log(Level.INFO,"Server response "+ response);
    }

    public static void delete_pair(String key, Accept_Client_Request_Int obj) throws Exception{

        ArrayList response = new ArrayList();
        ArrayList<String> requests = new ArrayList<>();                 //function to delete the pair at the server
        requests.add("del");
        requests.add(key);
        System.out.println("Sending Delete req" + requests);
        lgm.log(Level.INFO,"DEL-request sent to the server "+ requests);

        response = obj.remote_send_request(requests);        //log necessary information in the logger and accept response

        System.out.println("Server message: "+ response);
        lgm.log(Level.INFO,"Server response "+ response);
    }
}

//REF
//https://stackoverflow.com/questions/49186029/how-do-i-modify-a-log-format-with-simple-formatter
//https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html