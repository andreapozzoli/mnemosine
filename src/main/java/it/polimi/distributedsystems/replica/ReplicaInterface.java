package it.polimi.distributedsystems.replica;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface ReplicaInterface extends Remote {

    int notifyConnection(int replicaId) throws RemoteException;

    HashMap<String,Integer> getDB() throws RemoteException;
}
