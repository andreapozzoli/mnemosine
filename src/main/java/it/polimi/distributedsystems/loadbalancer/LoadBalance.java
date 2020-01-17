/**
 * 
 */
package it.polimi.distributedsystems.loadbalancer;

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
 * @author 87068
 *
 */
public class LoadBalance extends UnicastRemoteObject implements LoadBalanceInterface {

	private HashMap<String,Integer> workload;
	private int historyCounter;
	
	private File log = new File("backup.txt");

	public LoadBalance() throws RemoteException{
		workload = new HashMap<>();
		historyCounter = -1;
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

	protected String getReplica() {
		//get the key(IP:PORT) of minimum value(number of users)
		List<Entry<String,Integer>> list = new ArrayList<>(workload.entrySet());
		list.sort((o1, o2) -> (o1.getValue() - o2.getValue()));
		int i = list.size();
		System.out.println(i);
		double randNum = ThreadLocalRandom.current().nextDouble(0, 1);
		System.out.println(randNum);
		while (i>0) {
			System.out.println(i);
			if (randNum < 1.0/i) {
				System.out.println("entrato");
				return list.get(i-1).getKey();
			}
			i--;
		}
		System.out.println("uscito");
		return list.get(0).getKey();
	}

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
		workload.remove(name);
		writeLog();
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
	public int getID() {
		if(workload.size() == 0) {
			historyCounter = -1;
		}
		historyCounter++;
		writeLog();
		return historyCounter;
	}
	
	private void writeLog() {
		try {
			FileWriter fw = new FileWriter(log);
			fw.write(String.valueOf(historyCounter));
			fw.flush();
			for (Entry<String, Integer> entry : workload.entrySet()) {
				fw.append("\n"+entry.getKey()+","+entry.getValue().toString());
				fw.flush();
			}
			fw.close();
		} catch (IOException e) {
			System.out.println("Impossible to update log file");
		}
		
	}
}
