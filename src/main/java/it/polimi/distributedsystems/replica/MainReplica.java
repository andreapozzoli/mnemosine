package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.loadbalancer.LoadBalancer;
import it.polimi.distributedsystems.loadbalancer.LoadBalancerInterface;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MainReplica {

	public static void main(String[] args) {

		LoadBalancerInterface lb = null;
		Registry registry = null;
		Replica rep = null;

		String myIP, registryIP;
		int myPort = 0;

		try {
			registryIP = args[0];
			myIP = args[1];
		} catch (IndexOutOfBoundsException e){
			registryIP = null;
			myIP = "localhost";
		}

		try {
			// Exporting the object of implementation class
			// (here we are exporting the remote object to the stub)

			// Binding the remote object (stub) in the registry
			registry = LocateRegistry.getRegistry(registryIP,Registry.REGISTRY_PORT);
			//TODO: Split connectReplica in two methods, remeber to start from 0!
			lb = (LoadBalancerInterface) registry.lookup("LoadBalancer");
			myPort = 35000 + lb.connectReplica(myIP,1000);

			rep= new Replica(myPort - 35000);
			registry.bind("Rep_"+(myPort - 35000), rep);
			System.out.println("Replica N°" + registry.list().length + "has been exposed");

			rep.collectNeighbors(registry);

		} catch (RemoteException | AlreadyBoundException | NotBoundException e){
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
			System.exit(10);
		}

		//TODO: Open Socket and wait for clients

		//TODO: New Tread for wait input
		boolean endSignal = false;
		while(!endSignal) {
			try {
				lb.disconnectReplica(myIP,myPort);
				endSignal = true;
				registry.unbind("Rep_"+(myPort - 35000));
			} catch (RemoteException | NotBoundException e) {
				endSignal = false;
			}
		}


	}

}
