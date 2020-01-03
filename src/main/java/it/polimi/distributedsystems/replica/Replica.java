package it.polimi.distributedsystems.replica;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;


public class Replica extends UnicastRemoteObject implements ReplicaInterface{

	private ArrayList<ReplicaInterface> neighbour = new ArrayList<>();
	private ArrayList<Integer> vectorClock = new ArrayList<>();
	private Registry rmi;

	private int id;

	private HashMap<String,Integer> dataBase = new HashMap<>();

	public Replica(int id, Registry registry) throws RemoteException {
		this.id = id;
		rmi = registry;
	}

	protected void collectNeighbors() {
		int i = 0;

		for(; i < id; i++) {
			try {
				ReplicaInterface replica = (ReplicaInterface) rmi.lookup("Rep_"+i);
				neighbour.add(replica);
				vectorClock.add(replica.notifyConnection(id));

			} catch (RemoteException e){
				System.out.println("Registry isn't available, I'm shutting down");
				System.exit(10);
			} catch (NotBoundException e) {
				neighbour.add(null);
				vectorClock.add(0); //Others should put a 0 when replica died or not waiting for message from deadReplicas
			}
		}

		neighbour.add(null);
		vectorClock.add(0); //My Clock

		i = 0;
		while(neighbour.get(i) == null && i<id) {
			i++;
		}
		if(i != id) {
			try {
				dataBase = neighbour.get(i).getDB();
			} catch (RemoteException e) {
				System.out.println("Failed to pull more recent DB");
			}
		}
	}

	@Override
	public int notifyConnection(int replicaId) {
		System.out.println(replicaId + " asked to connect");
		 for(int i = neighbour.size(); i<=replicaId; i++) {
			 try {
			 	ReplicaInterface replica = (ReplicaInterface) rmi.lookup("Rep_"+i);
				neighbour.add(replica);
				vectorClock.add(replica.notifyConnection(id));
			 } catch (RemoteException | NotBoundException e) {
				System.out.println("Cannot locate this replica! Adding null");
				neighbour.add(null);
				vectorClock.add(0);
			 }
		 }
		 System.out.println(vectorClock);
		 System.out.println(neighbour);
		 return (vectorClock.contains(id)) ? vectorClock.get(id) : 0;
	}

	@Override
	public HashMap<String, Integer> getDB() {
		return dataBase;
	}

}
