package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.loadbalancer.LoadBalancerInterface;

import java.lang.reflect.Array;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Replica extends UnicastRemoteObject implements ReplicaInterface{

	private ArrayList<ReplicaInterface> neighbour = new ArrayList<>();
	private ArrayList<Integer> deadReplicasIndexs = new ArrayList<>();
	private ArrayList<Integer> vectorClock = new ArrayList<>();

	private int id;

	private HashMap<String,Integer> dataBase = new HashMap<>();

	public Replica(int id) throws RemoteException {
		this.id = id;
	}

	protected void collectNeighbors(Registry registry) {
		int i = 0;
		for(; i < id; i++) {
			try {
				ReplicaInterface replica = (ReplicaInterface) registry.lookup("Rep_"+i);
				neighbour.add(replica);
				vectorClock.add(replica.notifyConnection(id));

			} catch (RemoteException e){
				System.out.println("Registry isn't available, I'm shutting down");
				//TODO: be sure you shutdown gracefully
				//System.exit(10);
			} catch (NotBoundException e) {
				neighbour.add(null);
				deadReplicasIndexs.add(i);
				vectorClock.add(0); //Others should put a 0 when replica died or not waiting for message from deadReplicas
			}
		}
		neighbour.add(null);
		deadReplicasIndexs.add(i);
		vectorClock.add(0); //My Clock

		i = 0;
		while(neighbour.get(i) == null && i<id) {
			i++;
		}
		if(i != id) {
			dataBase = neighbour.get(i).getDB();
		}
	}

	@Override
	public int notifyConnection(int replicaId) {
		//TODO compleate the method with binding neighbour.add(), use the id, fill the holes with nulls
		return vectorClock.get(id);
	}

	@Override
	public HashMap<String, Integer> getDB() {
		return dataBase;
	}

}
