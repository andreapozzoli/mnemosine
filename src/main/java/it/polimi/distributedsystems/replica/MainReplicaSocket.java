package it.polimi.distributedsystems.replica;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainReplicaSocket implements Runnable {

    private ServerSocket serverSocket = null;
    private final ExecutorService threadExecutor = Executors.newFixedThreadPool(256);
    private final Replica rep;

    MainReplicaSocket(Replica replica, int port) {
        rep = replica;
        try {
            serverSocket = new ServerSocket(6970 + port - 35000);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(10);
        }
    }

    @Override
    public void run() {
        int k = 0;
        while(k < 2048) { // reachable end condition added
            k++;
            try {
                System.out.println("Waiting for the client request...");
                Socket socket = serverSocket.accept();
                threadExecutor.submit(new RepSocketClient(socket,rep));
                System.out.println("client" + socket + " accepted");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(10);
            }
        }
    }
}
