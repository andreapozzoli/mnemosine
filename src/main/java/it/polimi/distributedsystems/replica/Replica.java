package it.polimi.distributedsystems.replica;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class Replica extends UnicastRemoteObject implements ReplicaInterface{

	public Replica() throws RemoteException{
		
	}
}
