/**
 * 
 */
package it.polimi.distributedsystems.loadbalancer;

import java.rmi.server.UnicastRemoteObject;

import java.rmi.*;
import java.util.HashMap;

/**
 * @author 87068
 *
 */
public class LoadBalancer extends UnicastRemoteObject implements LoadBalancerInterface {

	//TODO: rendere singleton
	private HashMap<String,Integer> workload;
	private int historyCounter;

	public LoadBalancer() throws RemoteException{
		workload = new HashMap<>();
		historyCounter = 0;
	}

	protected String getReplica() {
		//Algoritmo di scelta.
		return workload.keySet().iterator().next();
	}

	protected boolean checkStatus(String id) {
		return workload.containsKey(id);
	}

	@Override
	public void disconnectReplica(String ip, int port){
		String name = ip+":"+port;
		workload.remove(name);
		System.out.println("Replica "+ name + " is shutting down");
	}

	@Override
	public int connectReplica(String ip, int port) {
		String name = ip+":"+port;
		workload.put(name,0);
		System.out.println("Replica "+ name + " is now connected");
		historyCounter++;
		return historyCounter;
	}

	@Override
	public void setWorkload(String id, int connectedClients) {
		workload.replace(id,connectedClients);
		System.out.println("Replica "+ id + " has now "+ connectedClients +" connected client");
	}
}
