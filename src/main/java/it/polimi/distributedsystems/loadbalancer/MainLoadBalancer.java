/**
 * 
 */
package it.polimi.distributedsystems.loadbalancer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author 87068
 *
 */
public class MainLoadBalancer {

	/**
	 * @param args, Few parameters
	 */
	public static void main(String[] args) {
		LoadBalancer obj = null;

		try {
			obj= new LoadBalancer();

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

		//Open Socket and wait for clients
		ServerSocket serverSocket = null;
		final ExecutorService threadExecutor = Executors.newFixedThreadPool(256);

		try {
			serverSocket = new ServerSocket(6969);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(10);
		}

		int j = 0;
		while(j < 2048) { // reachable end condition added
			j++;
			try {
				System.out.println("Waiting for the client request...");
				Socket socket = serverSocket.accept();
				threadExecutor.submit(new LBSocketClient(socket,obj));
				System.out.println("client" + socket + " accepted");
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(10);
			}
		}
	}

}
