package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.loadbalancer.LoadBalancerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import static it.polimi.distributedsystems.replica.MainReplica.PORT_SHIFT;


public class Replica {

	private String ip;
	private int id;


	private HashMap<String,Integer> dataBase = new HashMap<>();

	public Replica(int port, String ip) {
		this.id = port - PORT_SHIFT;
		this.ip = ip;
	}


	protected int getID(){ return id; }

	protected String getIP(){ return ip; }

	protected HashMap<String, Integer> getDB() {
		return dataBase;
	}

	protected void setDB(HashMap<String, Integer> db) {
		if (dataBase.isEmpty()) {
			dataBase = db;
		}
	}



	protected Integer read(String variable) {
		return dataBase.getOrDefault(variable, null);
	}

	protected boolean write(String variable, int value) {
		dataBase.put(variable, value);
		//TODO: need to discuss it with @Andrea
		return true;
	}

	protected boolean delete(String variable) {
		dataBase.remove(variable);
		//TODO: need to discuss it with @Andrea
		return true;
	}
}
