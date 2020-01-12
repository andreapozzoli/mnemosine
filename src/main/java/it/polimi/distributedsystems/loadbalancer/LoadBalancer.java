/**
 * 
 */
package it.polimi.distributedsystems.loadbalancer;

import java.rmi.server.UnicastRemoteObject;

import java.rmi.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map.Entry;

/**
 * @author 87068
 *
 */
public class LoadBalancer extends UnicastRemoteObject implements LoadBalancerInterface {

	private HashMap<String,Integer> workload;
	private int historyCounter;
	private HashMap<Integer, String> replicaId;

	public LoadBalancer() throws RemoteException{
		workload = new HashMap<>();
		historyCounter = -1;
		replicaId = new HashMap<>();
	}

	protected String getReplica() {
		//get the key(IP:PORT) of minimum value(number of users)
		List<Entry<String,Integer>> list = new ArrayList<>(workload.entrySet());
		list.sort((o1, o2) -> (o1.getValue() - o2.getValue()));
		return list.get(0).getKey();
	}

	protected boolean checkStatus(String id) {
		return workload.containsKey(id);
	}
	
	@Override
	public boolean checkStatusReplica(int id) {
		String key = replicaId.get(id);
		return workload.containsKey(key);
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
		replicaId.put(port-6970, name);
		System.out.println("Replica "+ name + " is now connected");
	}

	@Override
	public void setWorkload(String id, int connectedClients) {
		workload.replace(id,connectedClients);
		System.out.println("Replica "+ id + " has now "+ connectedClients +" connected client");
	}

	@Override
	public int getID(String ip) {
		if(workload.size() == 0) {
			historyCounter = -1;
		}
		historyCounter++;
		return historyCounter;
	}
}
