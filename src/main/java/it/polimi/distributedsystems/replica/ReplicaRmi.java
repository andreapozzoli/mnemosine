package it.polimi.distributedsystems.replica;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class ReplicaRmi extends UnicastRemoteObject implements ReplicaInterface {

    private ArrayList<ReplicaInterface> neighbour = new ArrayList<>();
    private ArrayList<Integer> vectorClock = new ArrayList<>();
    private final Replica replica;

    private String registryIP;

    protected ReplicaRmi(String registryIp, Replica replica) throws RemoteException {
        registryIP = registryIp;
        this.replica = replica;
    }

    protected void collectNeighbors() {
        int id = replica.getID();
        int i = 0;

        Registry rmi = null;
        try {
            rmi = LocateRegistry.getRegistry(registryIP, Registry.REGISTRY_PORT);
        } catch (RemoteException e) {
            System.out.println("Registry isn't available, I'm shutting down");
            System.exit(10);
        }

        for(; i < id; i++) {
            try {
                ReplicaInterface replica = (ReplicaInterface) rmi.lookup("Rep_"+i);
                neighbour.add(replica);
                vectorClock.add(replica.notifyConnection(id));

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

        Registry rmi = null;
        try {
            rmi = LocateRegistry.getRegistry(registryIP, Registry.REGISTRY_PORT);
        } catch (RemoteException e) {
            System.out.println("Registry isn't available, I'm shutting down");
            System.exit(10);
        }

        System.out.println(replicaId + " asked to connect");
        for(int i = neighbour.size(); i<=replicaId; i++) {
            try {
                ReplicaInterface rep = (ReplicaInterface) rmi.lookup("Rep_"+i);
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
        return (vectorClock.contains(replica.getID())) ? vectorClock.get(replica.getID()) : 0;
    }

    @Override
    public HashMap<String, Integer> pullDB() throws RemoteException {
        return replica.getDB();
    }
}
