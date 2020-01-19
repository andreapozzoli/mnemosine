package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.loadbalancer.LoadBalanceInterface;

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
		LoadBalanceInterface lb = null;
		Registry localRegistry = null;

		String myIP, nameServiceIP;
		int myPort = 0;

		try {
			nameServiceIP = args[0];
			myIP = args[1];
			System.setProperty("java.rmi.server.hostname", myIP);
		} catch (IndexOutOfBoundsException e){
			nameServiceIP = "localhost";
			myIP = "localhost";
		}

		try {
			localRegistry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

		} catch (RemoteException e) {
			try {
				localRegistry = LocateRegistry.getRegistry(myIP,Registry.REGISTRY_PORT);
				//localRegistry.lookup("Test");
			} catch (RemoteException ex) {
				System.out.println("Couldn't get or create the local Registry, I'm shutting down");
				System.exit(500);
			}
		} //catch (NotBoundException ignored) {}

		try {
			// Get the LoadBalancer
			Registry nameService = LocateRegistry.getRegistry(nameServiceIP,Registry.REGISTRY_PORT);
			lb = (LoadBalanceInterface) nameService.lookup("LoadBalancer");


			myPort = PORT_SHIFT + lb.getID();
			Replica rep = new Replica(myPort,myIP);
			repRmi = new ReplicaRmi(nameServiceIP,rep);

			localRegistry.bind("Rep_" + (myPort-PORT_SHIFT), repRmi);
			System.out.println("Replica N°" + (myPort - PORT_SHIFT) + " has been exposed");

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
		System.exit(0);

	}

}
