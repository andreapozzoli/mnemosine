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
import java.util.*;
import java.util.Map.Entry;

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
		//get the key(IP) of minimum value(number of users)
		List<Map.Entry<String,Integer>> list = new ArrayList<Entry<String, Integer>>(workload.entrySet());
		Collections.sort(list, (o1, o2) -> (o1.getValue() - o2.getValue()));
		String IP_free=list.get(0).getKey();	
		return IP_free;
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
