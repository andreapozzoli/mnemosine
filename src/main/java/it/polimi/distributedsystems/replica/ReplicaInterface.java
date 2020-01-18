package it.polimi.distributedsystems.replica;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface ReplicaInterface extends Remote {

    int notifyConnection(int replicaId) throws RemoteException;

    HashMap<String,Integer> pullDB() throws RemoteException;
    
    void writeFromReplica(String variable, int value, ArrayList<Integer> vector, String type, int senderId) throws RemoteException;
}
