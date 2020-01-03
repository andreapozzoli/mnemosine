package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.loadbalancer.LoadBalancer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class SocketClient implements Runnable  {

    /**
     * Socket to listen
     */
    private final Socket socket;
    /**
     * Reader from the network
     */
    private Scanner in;
    /**
     * Writer for the socket
     */
    private final PrintStream out;
    /**
     * LoadBalancer
     */
    private final Replica master;

    SocketClient(Socket socket, Replica replica) throws IOException {
        this.socket = socket;
        in = new Scanner(socket.getInputStream());
        out = new PrintStream(socket.getOutputStream());
        master = replica;
    }

    @Override
    public void run() {
        int j = Integer.MIN_VALUE;
        try {
            do {
                j++;

                in.nextLine();

            } while (j != Integer.MIN_VALUE);
        } catch (NoSuchElementException e) {
            try {
                socket.close();
                System.out.println("Socket is closed");
            } catch (IOException err) {
                System.out.println("Impossible to close the connection!");
            }
        }
    }
}
