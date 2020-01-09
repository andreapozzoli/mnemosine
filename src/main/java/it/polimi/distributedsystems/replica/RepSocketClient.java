package it.polimi.distributedsystems.replica;

import it.polimi.distributedsystems.client.SocketClient;
import it.polimi.distributedsystems.loadbalancer.LoadBalancerInterface;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;


public class RepSocketClient extends SocketClient  {

    private final Replica replica;
    private final JSONObject errorMessage;
    private int clients;
    private String registryIP;


    RepSocketClient(Socket socket, Replica rep, String registry) throws IOException {
        super(socket);
        registryIP = registry;
        replica = rep;
        errorMessage = new JSONObject(new HashMap<String,String>(){
            { put("method","ERROR"); put("content","Internal Server Error, Retry"); }
        });

        clientConnection();
    }

    @Override
    protected void decode(String input) {
        JSONParser jsonParser = new JSONParser();

        HashMap<String,String> response = new HashMap<>();
        response.put("method","RESPONSE");

        try {
            JSONObject obj = (JSONObject) jsonParser.parse(input);
            switch (obj.get("method").toString()) {
                case "READ" :
                    response.put("resource",obj.get("resource").toString());
                    response.put("content",String.valueOf(replica.read(obj.get("resource").toString())));
                    break;
                case "WRITE" :
                    response.put("resource",obj.get("resource").toString());
                    response.put("content",Boolean.toString(
                            replica.write(obj.get("resource").toString(),Integer.parseInt(obj.get("content").toString()))
                    ));
                    break;
                case "DELETE" :
                    response.put("resource",obj.get("resource").toString());
                    response.put("content",Boolean.toString(replica.delete(obj.get("resource").toString())));
                    break;
                default:
                    throw new ParseException(404,"No Method Found");

            }
        } catch (ParseException e) {
            System.out.println("FAILED TO READ INCOMING JSON, send a NACK to client");
            System.out.println(errorMessage.toJSONString());
            super.send(errorMessage.toJSONString());
        }

        JSONObject res = new JSONObject(response);
        super.send(res.toJSONString());
    }

    protected void clientConnection() {
        clients++;
        Registry rmi = null;
        try {
            rmi = LocateRegistry.getRegistry(registryIP, Registry.REGISTRY_PORT);
            LoadBalancerInterface lb = (LoadBalancerInterface) rmi.lookup("LoadBalancer");
            lb.setWorkload(replica.getIP()+":"+replica.getID(),clients);
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Registry isn't available, save the status and moving forward");
        }
    }
}
