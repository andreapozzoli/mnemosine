package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.loadbalancer.LoadBalancerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;


public class Replica {

	private String ip;
	private int id;


	private HashMap<String,Integer> dataBase = new HashMap<>();

	public Replica(int id, String ip) {
		this.id = id;
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



	protected int read(String variable) {
		return dataBase.getOrDefault(variable, -1);
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
