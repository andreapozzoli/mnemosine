package it.polimi.distributedsystems.loadbalancer;

import it.polimi.distributedsystems.replica.ReplicaInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import static it.polimi.distributedsystems.replica.MainReplica.PORT_SHIFT;

/**
 * Used to manage replicas workload and RMI registry
 */
public class LoadBalance extends UnicastRemoteObject implements LoadBalanceInterface {

	private HashMap<String,Integer> workload;
	private int historyCounter;
	private File log = new File("backup.txt");

	/**
	 * @throws RemoteException When the connection drop
	 */
	public LoadBalance() throws RemoteException{
		workload = new HashMap<>();
		historyCounter = -1;

		readLog();
	}

	/**
	 * @return ID("ip:port") of an available replica with a unique random distribution.
	 */
	protected String getReplica() {
		//get the key(IP:PORT) of minimum value(number of users)
		List<Entry<String,Integer>> list = new ArrayList<>(workload.entrySet());
		list.sort(Comparator.comparingInt(Entry::getValue));
		int i = list.size();

		double randNum = ThreadLocalRandom.current().nextDouble(0, 1);
		while (i>0) {
			if (randNum < 1.0/i) {
				System.out.println("Client ask for a partner, I give him replica n°"+ (i-1));
				return list.get(i-1).getKey();
			}
			i--;
		}
		return list.get(0).getKey();
	}

	/**
	 * @param id Replica ID
	 * @return True if the replica didn't disconnect from the LoadBalancer, False otherwise
	 */
	protected boolean checkStatus(String id) {
		return workload.containsKey(id);
	}
	
	@Override
	public boolean checkStatusReplica(int id) {
		String searchID = ":" + (id + PORT_SHIFT);
		for(String key: workload.keySet()) {
			if(key.contains(searchID)){
				return true;
			}
		}
		System.out.println("Someone search for Rep_"+ id +" but it is not online");
		return false;

	}

	@Override
	public void disconnectReplica(String ip, int port){
		String name = ip+":"+port;
		int id = port - PORT_SHIFT;
		if (workload.remove(name) != null)  {
			unbindRemoteReplica(id);
			System.out.println("Replica "+ name + " is now disconnected");
			writeLog();
		}
	}

	@Override
	public void connectReplica(String ip, int port) {
		String name = ip+":"+port;
		workload.put(name,0);
		System.out.println("Replica "+ name + " is now connected");
		writeLog();
	}

	@Override
	public void setWorkload(String id, int var) {
		workload.putIfAbsent(id, 0);		
		workload.replace(id,workload.get(id)+var);
		System.out.println("Replica "+ id + " has now "+ workload.get(id) +" connected client");
		writeLog();
	}

	@Override
	public void bindRemoteReplica(ReplicaInterface replica) throws RemoteException, AlreadyBoundException {
		Registry registry = LocateRegistry.getRegistry("localhost",Registry.REGISTRY_PORT);
		registry.bind("Rep_"+replica.getID(), replica);
		System.out.println("Replica N°" + replica.getID() + " has been exposed");
	}

	private void unbindRemoteReplica(int id) {
		Registry registry = null;
		try {
			registry = LocateRegistry.getRegistry("localhost", Registry.REGISTRY_PORT);
			registry.unbind("Rep_"+ id);
			System.out.println("Replica N°" + id + " is not attached anymore");
		} catch (RemoteException | NotBoundException e) {
			System.out.println("Registry not available, shutdown is not possible");
		}
		
	}

	@Override
	public int getID() {
		if(workload.size() == 0) {
			historyCounter = -1;
		}
		historyCounter++;
		System.out.println("Replica got ID=" + historyCounter);

		writeLog();

		return historyCounter;
	}


	private void readLog() {
		if (log.exists()) {
			try {
				Scanner myReader = new Scanner(log);
				historyCounter = Integer.parseInt(myReader.nextLine().replaceAll("\n", ""));
				while (myReader.hasNextLine()) {
					String[] line = myReader.nextLine().replaceAll("\n", "").split(",");
					workload.put(line[0], Integer.parseInt(line[1]));
				}
				myReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			System.out.println("Founded a previous backup... Recovering Done!");
		}
		else {
			try {
				log.createNewFile();
			} catch (IOException e) {
				System.out.println("Unable to create log file...");
				System.out.println("Shutting down...Retry!!");
				System.exit(500);
			}
		}

	}
	
	private void writeLog() {
		try {
			FileWriter fw = new FileWriter(log);
			fw.write(String.valueOf(historyCounter));
			fw.flush();
			for (Entry<String, Integer> entry : workload.entrySet()) {
				fw.append("\n").append(entry.getKey()).append(",").append(entry.getValue().toString());
				fw.flush();
			}
			fw.close();
		} catch (IOException e) {
			System.out.println("Impossible to update log file");
		}
		System.out.println("Log successfully updated");
		
	}
}
