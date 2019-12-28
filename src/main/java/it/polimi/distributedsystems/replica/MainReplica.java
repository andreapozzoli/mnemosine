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

		String myIP, registryIP;
		try {
			registryIP = args[0];
			myIP = args[1];
		} catch (IndexOutOfBoundsException e){
			registryIP = null;
			myIP = "localhost";
		}

		try {
			Replica obj= new Replica();

			// Exporting the object of implementation class
			// (here we are exporting the remote object to the stub)

			// Binding the remote object (stub) in the registry
			Registry registry = LocateRegistry.getRegistry(registryIP,Registry.REGISTRY_PORT);
			registry.bind("Rep_"+registry.list().length,obj);
			System.out.println("Replica N°" + registry.list().length + "has been exposed");
		} catch (RemoteException | AlreadyBoundException e){
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		//TODO: Open Socket and wait for clients

	}

}
