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

		LoadBalancer obj= new LoadBalancer();
		System.out.println("CheckPoint DeBug");

		try {
			// Exporting the object of implementation class
			// (here we are exporting the remote object to the stub)
			LoadBalancerInterface stub = (LoadBalancerInterface) UnicastRemoteObject.exportObject(obj, 15800);
			System.out.println("Load Balancer exposed on port 15800");


			// Binding the remote object (stub) in the registry
			Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			registry.bind("LoadBalancer",stub);
		} catch (RemoteException | AlreadyBoundException e){
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		//TODO: Open Socket and wait for clients
	}

}
