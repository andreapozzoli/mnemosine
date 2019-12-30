package it.polimi.distributedsystems.replica;

import java.rmi.Remote;
import java.util.HashMap;

public interface ReplicaInterface extends Remote {

    int notifyConnection (int replicaId);

    HashMap<String,Integer> getDB();
}
