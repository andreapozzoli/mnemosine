package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.loadbalancer.LoadBalancerInterface;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.*;

public class MainReplica {

	public static final int PORT_SHIFT = 6970;

	public static void main(String[] args) {

		ReplicaRmi repRmi = null;
		String myIP, registryIP;
		int myPort = 0;

		try {
			registryIP = args[0];
			myIP = args[1];
		} catch (IndexOutOfBoundsException e){
			registryIP = "localhost";
			myIP = "localhost";
		}

		try {
			// Exporting the object of implementation class
			// (here we are exporting the remote object to the stub)

			// Binding the remote object (stub) in the registry
			Registry registry = LocateRegistry.getRegistry(registryIP,Registry.REGISTRY_PORT);

			LoadBalancerInterface lb = (LoadBalancerInterface) registry.lookup("LoadBalancer");
			myPort = PORT_SHIFT + lb.getID(myIP);

			Replica rep = new Replica(myPort,myIP);
			repRmi = new ReplicaRmi(registryIP, rep);
			lb.connectReplica(myIP, myPort);
			registry.bind("Rep_"+(myPort - PORT_SHIFT), repRmi);
			System.out.println("Replica N°" + (myPort - PORT_SHIFT) + " has been exposed");

			repRmi.collectNeighbors();

		} catch (RemoteException | AlreadyBoundException | NotBoundException e){
			System.err.println("Server exception: " + e.toString());
			System.exit(10);
		}

		final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

		/*+++++++++++++++++*
		 * SOCKET LISTENER *
		 *+++++++++++++++++*/
		Thread doHandshake = new Thread(new MainReplicaSocket(repRmi, myPort, registryIP));
		doHandshake.start();

		/*++++++++++++++++*
		 * INPUT LISTENER *
		 *++++++++++++++++*/
		//New Tread for wait input
		boolean endSignal = false;
		while(!endSignal) {
			System.err.println("Type Y to shutdown the replica: ");
			Scanner scan = new Scanner(System.in);
			Future<String> response = threadExecutor.submit((Callable<String>) scan::next);
			try {
				if(response.get().equalsIgnoreCase("Y")){
					Registry rmi = LocateRegistry.getRegistry(registryIP, Registry.REGISTRY_PORT);
					LoadBalancerInterface lb = (LoadBalancerInterface) rmi.lookup("LoadBalancer");
					lb.disconnectReplica(myIP,myPort);
					endSignal = true;
					rmi.unbind("Rep_"+(myPort - PORT_SHIFT));
				}
			} catch (RemoteException | InterruptedException | ExecutionException e) {
				System.out.println("Registry not available, shutdown is not possible");
				endSignal = false;
			} catch ( NotBoundException ignored) {

			}
		}
		System.exit(0);

	}

}
