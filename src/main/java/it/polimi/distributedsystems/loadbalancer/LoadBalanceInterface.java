package it.polimi.distributedsystems.loadbalancer;

import it.polimi.distributedsystems.replica.ReplicaInterface;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface of the LoadBalancer, exposed methods
 */
public interface LoadBalanceInterface extends Remote {

    //IDENTIFICATION = "ip:port"
    //ID = port - 6970

    /**
     * @param ip Replica IP
     * @param port Replica Socket listening port
     * @throws RemoteException When the connection drop
     */
    void disconnectReplica(String ip, int port) throws RemoteException;

    /**
     * @param ip Replica IP
     * @param port Replica Socket listening port
     * @throws RemoteException When the connection drop
     */
    void connectReplica(String ip, int port) throws RemoteException;

    /**
     * @param identification Replica IDENTIFICATION
     * @param variation How many clients connected since the last time
     * @throws RemoteException When the connection drop
     */
    void setWorkload(String identification, int variation) throws RemoteException;

    /**
     * @param replica
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    void bindRemoteReplica(ReplicaInterface replica) throws RemoteException, AlreadyBoundException;


    /**
     * @return new generated unique ID
     * @throws RemoteException When the connection drop
     */
    int getID() throws RemoteException;

    /**
     * @param id Replica ID
     * @return True if the replica is up and running, False otherwise
     * @throws RemoteException When the connection drop
     */
    boolean checkStatusReplica(int id) throws RemoteException;


}
