package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.loadbalancer.LoadBalanceInterface;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.*;

public class MainReplica {

	public static final int PORT_SHIFT = 6970;


	public static void main(String[] args) {

		ReplicaRmi repRmi = null;
		LoadBalanceInterface lb = null;

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
			// Binding the remote object (stub) in the registry
			Registry registry = LocateRegistry.getRegistry(registryIP,Registry.REGISTRY_PORT);

			lb = (LoadBalanceInterface) registry.lookup("LoadBalancer");
			myPort = PORT_SHIFT + lb.getID();

			Replica rep = new Replica(myPort,myIP);
			repRmi = new ReplicaRmi(registryIP, rep);
			lb.connectReplica(myIP, myPort);

			if (registryIP.equals(myIP)) {
				registry.bind("Rep_"+(myPort - PORT_SHIFT), repRmi);
			} else {
				lb.bindRemoteReplica(repRmi);
			}
			System.out.println("Replica N°" + (myPort - PORT_SHIFT) + " has been exposed");

			repRmi.collectNeighbors();

		} catch (RemoteException | AlreadyBoundException | NotBoundException e){
			System.err.println("Server exception: " + e.toString());
			try {
				assert lb != null;
				lb.disconnectReplica(myIP, myPort);
			} catch (RemoteException ee) {
				System.err.println("Server exception: " + ee.toString());
				System.exit(500);
			}
			System.exit(500);
		}

		/*+++++++++++++++++*
		 * SOCKET LISTENER *
		 *+++++++++++++++++*/
		Thread doHandshake = new Thread(new MainReplicaSocket(repRmi, myPort, registryIP));
		doHandshake.start();

		/*++++++++++++++++*
		 * INPUT LISTENER *
		 *++++++++++++++++*/
		final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
		//New Tread for wait input
		boolean endSignal = false;
		while(!endSignal) {
			System.err.println("Type Y to shutdown the replica: ");
			Scanner scan = new Scanner(System.in);
			Future<String> response = threadExecutor.submit((Callable<String>) scan::next);
			try {
				if(response.get().equalsIgnoreCase("Y")){
					Registry rmi = LocateRegistry.getRegistry(registryIP, Registry.REGISTRY_PORT);
					lb = (LoadBalanceInterface) rmi.lookup("LoadBalancer");
					lb.disconnectReplica(myIP,myPort);
					endSignal = true;
					System.out.println("Replica is now disconnected");
				}
			} catch (RemoteException | InterruptedException | ExecutionException e) {
				System.out.println("Registry not available, shutdown is not possible");
				endSignal = false;
			} catch ( NotBoundException ignored){}
		}
		System.exit(0);

	}

}
