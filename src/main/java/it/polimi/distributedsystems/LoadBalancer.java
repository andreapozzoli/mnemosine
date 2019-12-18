/**
 * 
 */
package it.polimi.distributedsystems;

import java.rmi.server.UnicastRemoteObject;

import java.rmi.*;

/**
 * @author 87068
 *
 */
public class LoadBalancer extends UnicastRemoteObject implements LoadBalancerInterface {
	public LoadBalancer() throws RemoteException{
		
	}

}
