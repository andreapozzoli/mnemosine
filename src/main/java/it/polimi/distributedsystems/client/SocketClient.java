package it.polimi.distributedsystems.client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;


public abstract class SocketClient implements Runnable  {

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

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        in = new Scanner(socket.getInputStream());
        out = new PrintStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        int j = Integer.MIN_VALUE;
        try {
            do {
                j++;

                decode(in.nextLine());

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

    protected abstract void decode(String input);


    protected synchronized void send(String response) {
        if (!socket.isClosed()) {
            out.println(response);
            out.flush();
        }
    }
}
