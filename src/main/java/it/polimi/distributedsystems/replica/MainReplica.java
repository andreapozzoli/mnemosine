package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.loadbalancer.LoadBalanceInterface;

import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
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
		Registry localRegistry = null;

		String myIP, nameServiceIP;
		boolean registryOwner = false;
		int myPort = 0;

		try {
			myIP  = args[0];
			nameServiceIP = args[1];
			System.setProperty("java.rmi.server.hostname", myIP);
			System.out.println("RMI exported address: "+ myIP);
		} catch (IndexOutOfBoundsException e){
			nameServiceIP = "localhost";
			myIP = "localhost";
			try {
				System.out.println("RMI exported address not specified - DEFAULT: "+ java.net.InetAddress.getLocalHost());
			} catch (UnknownHostException ignored) {}
		}

		try {
			localRegistry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			registryOwner = true;
			System.out.println("Creating Local RMI Registry.... Done!");
		} catch (RemoteException e) {
			try {
				localRegistry = LocateRegistry.getRegistry(myIP,Registry.REGISTRY_PORT);
			} catch (RemoteException ex) {
				System.out.println("Couldn't get or create the local Registry, I'm shutting down");
				System.exit(500);
			}
		}

		System.out.println("Local registry is up and running!");

		try {
			// Get the LoadBalancer
			Registry nameService = LocateRegistry.getRegistry(nameServiceIP,Registry.REGISTRY_PORT);
			lb = (LoadBalanceInterface) nameService.lookup("LoadBalancer");


			myPort = PORT_SHIFT + lb.getID();
			Replica rep = new Replica(myPort,myIP);
			repRmi = new ReplicaRmi(nameServiceIP,rep);

			localRegistry.bind("Rep_" + (myPort-PORT_SHIFT), repRmi);
			System.out.println("Replica N°" + (myPort - PORT_SHIFT) + " has been exposed on local machine");

			lb.connectReplica(myIP, myPort);
			repRmi.collectNeighbors();

		} catch (RemoteException | AlreadyBoundException | NotBoundException e){
			System.err.println("Server exception: " + e.toString());
			try {
				assert lb != null;
				lb.disconnectReplica(myIP, myPort);
				localRegistry.unbind("Rep_"+ (myPort-PORT_SHIFT));
			} catch (RemoteException | NotBoundException ee) {
				System.err.println("Server exception: " + ee.toString());
				System.exit(500);
			}
			System.exit(500);
		}

		/*+++++++++++++++++*
		 * SOCKET LISTENER *
		 *+++++++++++++++++*/
		Thread doHandshake = new Thread(new MainReplicaSocket(repRmi, myPort, nameServiceIP));
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
					lb.disconnectReplica(myIP,myPort);
					localRegistry.unbind("Rep_" + repRmi.getID());
					endSignal = true;
					System.out.println("Replica is now disconnected");
				}
			} catch (RemoteException | InterruptedException | ExecutionException e) {
				System.out.println("Registry not available, shutdown is not possible");
				endSignal = false;
			} catch ( NotBoundException ignored){}
		}

		doHandshake.interrupt();

		if (registryOwner) {
			System.out.println("Replica is down");
			System.out.println("Running RMI Registry Instance... please don't stop.");
			Future<String> response = threadExecutor.submit(() -> {while(true){}});
			try {
				UnicastRemoteObject.unexportObject(repRmi,false);
				response.get();
			} catch (InterruptedException | ExecutionException | NoSuchObjectException e) {
				System.out.println("We got an internal server crash the local registry will be lost forever");
				System.exit(500);
			}
		}

		System.out.println("All threads are stopped, GoodBye.");
		System.exit(0);

	}

}
