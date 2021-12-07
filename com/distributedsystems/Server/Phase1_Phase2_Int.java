package com.distributedsystems.Server;

import java.rmi.Remote;
import java.util.ArrayList;

public interface Phase1_Phase2_Int extends Remote {
    String phase1(String response) throws Exception;

    ArrayList phase2(ArrayList request) throws Exception;
}
