package it.polimi.distributedsystems.loadbalancer;

import it.polimi.distributedsystems.client.SocketClient;

import java.io.IOException;
import java.net.Socket;


public class LBSocketClient extends SocketClient  {

    private final LoadBalancer loadBalancer;

    LBSocketClient(Socket socket, LoadBalancer lb) throws IOException {
        super(socket);
        loadBalancer = lb;
    }

    @Override
    protected void decode(String input) {
        //Json decode
    }
}
