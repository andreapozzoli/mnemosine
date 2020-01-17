/**
 * 
 */
package it.polimi.distributedsystems.loadbalancer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author 87068
 *
 */
public interface LoadBalanceInterface extends Remote {

    //ID = ip:port

    void disconnectReplica(String ip, int port) throws RemoteException;

    void connectReplica(String ip, int port) throws RemoteException;

    void setWorkload(String id, int variation) throws RemoteException;

    int getID() throws RemoteException;
    
    boolean checkStatusReplica(int id) throws RemoteException;


}