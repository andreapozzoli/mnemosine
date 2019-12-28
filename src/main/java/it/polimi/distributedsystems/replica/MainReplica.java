package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.loadbalancer.LoadBalancer;
import it.polimi.distributedsystems.loadbalancer.LoadBalancerInterface;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MainReplica {

	public static void main(String[] args) {
		Replica obj= new Replica();
		System.out.println("CheckPoint DeBug");
		String myIP, registryIP;
		try {
			registryIP = args[0];
		} catch (IndexOutOfBoundsException e){
			registryIP = null;
		}

		try {
			// Exporting the object of implementation class
			// (here we are exporting the remote object to the stub)
			ReplicaInterface stub = (ReplicaInterface) UnicastRemoteObject.exportObject(obj, 15801);
			System.out.println("Replica exposed on port 15801");


			// Binding the remote object (stub) in the registry
			Registry registry = LocateRegistry.getRegistry(registryIP,Registry.REGISTRY_PORT);
			registry.bind("Rep1",obj);
		} catch (RemoteException | AlreadyBoundException e){
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		//TODO: Open Socket and wait for clients

	}

}
