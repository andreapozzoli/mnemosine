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

import static it.polimi.distributedsystems.replica.MainReplica.PORT_SHIFT;


public class RepSocketClient extends SocketClient  {

    private final ReplicaRmi replica;
    private final JSONObject errorMessage;
    
    private String registryIP;


    RepSocketClient(Socket socket, ReplicaRmi rep, String registry) throws IOException {
        super(socket);
        registryIP = registry;
        replica = rep;
        errorMessage = new JSONObject(new HashMap<String,String>(){
            { put("method","ERROR"); put("content","Internal Server Error, Retry"); }
        });

        changeWorkload(1);
    }

    @Override
    protected void decode(String input) {
        JSONParser jsonParser = new JSONParser();

        HashMap<String,String> response = new HashMap<>();
        response.put("method","RESPONSE");

        try {
            JSONObject obj = (JSONObject) jsonParser.parse(input);
            String messageContent = obj.get("resource").toString().replaceAll("\n", "");

            switch (obj.get("method").toString()) {
                case "READ" :
                    response.put("resource",messageContent);
                    response.put("content", replica.read(messageContent));
                    break; 
                case "WRITE" :
                    response.put("resource",messageContent);
                    response.put("content",Boolean.toString(
                            replica.writeFromClient(messageContent,Integer.parseInt(obj.get("content").toString().replaceAll("\n", "")), "write")
                    ));
                    break;
                case "DELETE" :
                    response.put("resource",messageContent);
                    response.put("content",Boolean.toString(replica.writeFromClient(messageContent, 0, "delete")));
                    break;
                default:
                    throw new ParseException(404,"No Method Found");

            }
        } catch (ParseException e) {
            System.err.println("FAILED TO READ INCOMING JSON, send a NACK to client");
            System.out.println(errorMessage.toJSONString());
            super.send(errorMessage.toJSONString());
        }

        JSONObject res = new JSONObject(response);
        super.send(res.toJSONString());
    }

    @Override
    protected void close() {
        System.out.println("Socket died, client disconnected, I will forward the news to the loadBalancer");
        changeWorkload(-1);
    }

    protected void changeWorkload(int variation) {
        Registry rmi;
        try {
            rmi = LocateRegistry.getRegistry(registryIP, Registry.REGISTRY_PORT);
            LoadBalancerInterface lb = (LoadBalancerInterface) rmi.lookup("LoadBalancer");
            lb.setWorkload(replica.getIP()+":"+(replica.getID()+PORT_SHIFT),variation);
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Registry isn't available, my workload won't be registered");
        }
    }
}
