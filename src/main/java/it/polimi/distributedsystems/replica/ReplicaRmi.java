package it.polimi.distributedsystems.replica;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import it.polimi.distributedsystems.loadbalancer.LoadBalance;
import it.polimi.distributedsystems.loadbalancer.LoadBalanceInterface;

public class ReplicaRmi extends UnicastRemoteObject implements ReplicaInterface {

    private ArrayList<ReplicaInterface> neighbour = new ArrayList<>();
    private ArrayList<Integer> vectorClock = new ArrayList<>();
	private ArrayList<WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer>> waitingWrites = new ArrayList<>();
	private ArrayList<WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer>> pendingSendings = new ArrayList<>();

    private final Replica replica;
    private String loadBalacerIP;


    protected ReplicaRmi(String loadbalancer, Replica replica) throws RemoteException {
		loadBalacerIP = loadbalancer;
        this.replica = replica;
    }

    protected void collectNeighbors() {
        int id = replica.getID();
        int i = 0;

        LoadBalanceInterface lb = null;
        try {
            lb = (LoadBalanceInterface) LocateRegistry.getRegistry(loadBalacerIP, Registry.REGISTRY_PORT).lookup("LoadBalancer");
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Registry isn't available, I'm shutting down");
            System.exit(500);
        }

        for(; i < id; i++) {
            try {
            	String regIP = lb.getIP(i);
            	if (!(regIP.equals("NotFound"))) {
                    ReplicaInterface replica = (ReplicaInterface) LocateRegistry.getRegistry(regIP, Registry.REGISTRY_PORT).lookup("Rep_"+i);
                    neighbour.add(replica);
                    vectorClock.add(replica.notifyConnection(id));            		
            	}
            	else {
            		throw new NotBoundException();
            	}


            } catch (RemoteException e){
                System.out.println("Registry isn't available, I'm shutting down");
                System.exit(10);
            } catch (NotBoundException e) {
                neighbour.add(null);
                vectorClock.add(0); //Others should put a 0 when replica died or not waiting for message from deadReplicas
            }
        }

        neighbour.add(null);
        vectorClock.add(0); //My Clock

        i = 0;
        while(neighbour.get(i) == null && i<id) {
            i++;
        }
        if(i != id) {
            try {
                replica.setDB(neighbour.get(i).pullDB());
            } catch (RemoteException e) {
                System.out.println("Failed to pull more recent DB");
            }
        }

        System.out.println("Connected to " + (neighbour.size()-1) + " replicas");
        System.out.println("Vector Clock: " + vectorClock);
    }

    @Override
    public int notifyConnection(int replicaId) {

		LoadBalanceInterface lb = null;
		try {
			lb = (LoadBalanceInterface) LocateRegistry.getRegistry(loadBalacerIP, Registry.REGISTRY_PORT).lookup("LoadBalancer");
		} catch (RemoteException | NotBoundException e) {
            System.out.println("Registry isn't available, I'm shutting down");
            System.exit(500);
        }

        System.out.println("Rep_" + replicaId + " asked to connect");

        for(int i = neighbour.size(); i<=replicaId; i++) {
            try {
				String regIP = lb.getIP(i);
                ReplicaInterface rep = (ReplicaInterface) LocateRegistry.getRegistry(regIP, Registry.REGISTRY_PORT).lookup("Rep_"+i);
                neighbour.add(rep);
                vectorClock.add(rep.notifyConnection(replica.getID()));
            } catch (RemoteException | NotBoundException e) {
                System.out.println("Cannot locate this replica! Adding null");
                neighbour.add(null);
                vectorClock.add(0);
            }
        }

        System.out.println("DEBUG:");
        System.out.println(neighbour);
        System.out.println();

        return (vectorClock.size() >replica.getID()) ? vectorClock.get(replica.getID()) : 0;
    }

    @Override
    public HashMap<String, Integer> pullDB() throws RemoteException {
        return replica.getDB();
    }

	@Override
	public int getID(){ return replica.getID(); }



    public String read(String variable) {
    	Integer read = replica.read(variable);
    	return read==null ? "Not Found" : read.toString();
	}

	protected String getIP(){ return replica.getIP(); }
    
    public boolean writeFromClient(String variable, int value, String type) {
    	vectorClock.set(replica.getID(), vectorClock.get(replica.getID())+1);
    	for (int j=0; j<neighbour.size(); j++) {
    		boolean sent = false;
    		int countrep=0;
    		if (neighbour.get(j)!=null) {
    			while(!sent && countrep<2) {
        			try {
        				for (WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer> ps : getPendingSendings(j)) {
        					neighbour.get(j).writeFromReplica(ps.getFirst(), ps.getSecond(), ps.getThird(), ps.getFourth(), replica.getID());
        					pendingSendings.remove(ps);
        				}
        				neighbour.get(j).writeFromReplica(variable, value, vectorClock, type, replica.getID());
        				sent=true;
        			}
        			catch (RemoteException e) {
        				Registry registry;
        				boolean existing=true;
						try {
							registry = LocateRegistry.getRegistry(loadBalacerIP,Registry.REGISTRY_PORT);
							LoadBalanceInterface lb = (LoadBalanceInterface) registry.lookup("LoadBalancer");
							existing = lb.checkStatusReplica(j);
						} catch (RemoteException | NotBoundException e1) {
							System.out.println("RMI error, retry connection to loadBalancer");
						}
        				if (existing) {
        					System.out.println("retry with neighbour "+j);
        					countrep++;
        				}
        				else {
        					neighbour.set(j, null);
            				sent = true;
        				}
        			}

    			}
    			if (!sent) {
    				WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer> pendingSending = new WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer>(variable, value, vectorClock, type, j);
    				pendingSendings.add(pendingSending);
    			}
    		}
    	}
    	if (type.equals("write")) {
        	replica.write(variable, value);	
    	}
    	else if (type.equals("delete")) {
    		replica.delete(variable);
    	}
    	return true;
    }
    
    @Override
    public void writeFromReplica(String variable, int value, ArrayList<Integer> vector, String type, int senderId) {
    	boolean missing = false;
    	for (int k=0; k<neighbour.size(); k++) {
    		if (neighbour.get(k)!=null) {
    			if (k!=senderId) {
    				if (vector.get(k)>vectorClock.get(k)) {
    					missing = true;
    				}
    			}
    			else {
    				if (vector.get(k)!=vectorClock.get(k)+1) {
    					missing = true;
    				}
    			}
    		}
    	}
    	if (!missing) {
    		if (type.equals("write")) {
    			replica.write(variable, value);
    		}
    		else if (type.equals("delete")) {
    			replica.delete(variable);
    		}
    		vectorClock.set(senderId, vectorClock.get(senderId)+1);
    		boolean changed = true;
    		while (changed) {
    			changed = retryWrites();
    		}
    	}
    	else {
    		WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer> waitingWrite = new WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer>(variable, value, vector, type, senderId);
    		waitingWrites.add(waitingWrite);
    	}
    }
    
    private boolean retryWrites () {
    	boolean changed = false;
    	if (waitingWrites.isEmpty()) {
    		return false;
    	}
    	for (WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer> ww : waitingWrites) {
    		String variable = ww.getFirst();
    		int value = ww.getSecond();
    		ArrayList<Integer> vector = ww.getThird();
    		String type = ww.getFourth();
    		int senderId = ww.getFifth();
    		boolean missing = false;
        	for (int k=0; k<neighbour.size(); k++) {
        		if (neighbour.get(k)!=null) {
        			if (k!=senderId) {
        				if (vector.get(k)>vectorClock.get(k)) {
        					missing = true;
        				}
        			}
        			else {
        				if (vector.get(k)!=vectorClock.get(k)+1) {
        					missing = true;
        				}
        			}
        		}
        	}
        	if (!missing) {
        		changed = true;
        		if (type.equals("write")) {
        			replica.write(variable, value);
        		}
        		else if (type.equals("delete")) {
        			replica.delete(variable);
        		}
        		vectorClock.set(senderId, vectorClock.get(senderId)+1);
        		waitingWrites.remove(ww);
        		return true;
        	}
    	}
    	return changed;
    }
    
    private ArrayList<WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer>> getPendingSendings (int repId){
    	ArrayList<WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer>> repPendingSendings = new ArrayList<>();
    	for (WaitingWrite<String, Integer, ArrayList<Integer>, String, Integer> ps : pendingSendings) {
    		if (ps.getFifth()==repId) {
    			repPendingSendings.add(ps);
    		}
    	}
    	return repPendingSendings;
    }
}
