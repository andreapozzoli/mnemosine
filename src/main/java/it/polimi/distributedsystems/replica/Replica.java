package it.polimi.distributedsystems.replica;

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

	protected void write(String variable, int value) {
		dataBase.put(variable, value);
	}

	protected void delete(String variable) {
		dataBase.remove(variable);
	}
}
