/**
 * 
 */
package it.polimi.distributedsystems.loadbalancer;

import java.rmi.server.UnicastRemoteObject;

import java.rmi.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map.Entry;

import static it.polimi.distributedsystems.replica.MainReplica.PORT_SHIFT;

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
		String searchID = ":" + (id + PORT_SHIFT);
		for(String key: workload.keySet()) {
			if(key.contains(searchID)){
				return true;
			}
		}
		System.out.println("Someone search for Rep_"+ id +" but it is not online");
		return false;

	}

	@Override
	public void disconnectReplica(String ip, int port){
		String name = ip+":"+port;
		workload.remove(name);

	}

	@Override
	public void connectReplica(String ip, int port) {
		String name = ip+":"+port;
		workload.put(name,0);
		System.out.println("Replica "+ name + " is now connected");
	}

	@Override
	public void setWorkload(String id, int var) {
		workload.putIfAbsent(id, 0);		
		workload.replace(id,workload.get(id)+var);
		System.out.println("Replica "+ id + " has now "+ workload.get(id) +" connected client");
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
