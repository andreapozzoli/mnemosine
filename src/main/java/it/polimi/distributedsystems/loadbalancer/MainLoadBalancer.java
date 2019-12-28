/**
 * 
 */
package it.polimi.distributedsystems.loadbalancer;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author 87068
 *
 */
public class MainLoadBalancer {

	/**
	 * @param args, Few parameters
	 */
	public static void main(String[] args) {
		try {
			LoadBalancer obj= new LoadBalancer();

			// Exporting the object of implementation class
			// (here we are exporting the remote object to the stub)

			// Binding the remote object (stub) in the registry
			Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			registry.bind("LoadBalancer",obj);
			System.out.println("LoadBalancer exposed ");

		} catch (RemoteException | AlreadyBoundException e){
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		//TODO: Open Socket and wait for clients
	}

}
