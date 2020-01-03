package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.client.SocketClient;
import it.polimi.distributedsystems.loadbalancer.LoadBalancer;

import java.io.IOException;
import java.net.Socket;


public class RepSocketClient extends SocketClient  {

    private final Replica replica;

    RepSocketClient(Socket socket, Replica rep) throws IOException {
        super(socket);
        replica = rep;
    }

    @Override
    protected void decode(String input) {
        //Json decode
    }
}
