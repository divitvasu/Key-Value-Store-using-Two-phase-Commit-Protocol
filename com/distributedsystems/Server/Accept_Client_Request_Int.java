package com.distributedsystems.Server;

import java.rmi.Remote;
import java.util.ArrayList;

public interface Accept_Client_Request_Int extends Remote {

    ArrayList remote_send_request(ArrayList request) throws Exception;
}
