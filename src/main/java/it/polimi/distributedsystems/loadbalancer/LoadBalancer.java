/**
 * 
 */
package it.polimi.distributedsystems.loadbalancer;

import java.rmi.server.UnicastRemoteObject;

import java.rmi.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author 87068
 *
 */
public class LoadBalancer extends UnicastRemoteObject implements LoadBalancerInterface {

	private HashMap<String,Integer> workload;
	private int historyCounter;

	public LoadBalancer() throws RemoteException{
		workload = new HashMap<>();
		historyCounter = -1;
	}

	protected String getReplica() {
		int min = Collections.min(workload.values());
		ArrayList<String> possibleChoice = new ArrayList<>();
		for (String key : workload.keySet() ) {
			if (workload.get(key) == min) {
				possibleChoice.add(key);
			}
		}
		int choice = ThreadLocalRandom.current().nextInt(0,possibleChoice.size());
		return possibleChoice.get(choice);
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
	public void connectReplica(String ip, int port) {
		String name = ip+":"+port;
		workload.put(name,0);
		System.out.println("Replica "+ name + " is now connected");
	}

	@Override
	public void setWorkload(String id, int connectedClients) {
		workload.replace(id,connectedClients);
		System.out.println("Replica "+ id + " has now "+ connectedClients +" connected client");
	}

	@Override
	public int getID(String ip) {
		historyCounter++;
		return historyCounter;
	}
}
