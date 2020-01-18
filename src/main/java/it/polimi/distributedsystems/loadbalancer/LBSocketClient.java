package it.polimi.distributedsystems.loadbalancer;

import it.polimi.distributedsystems.client.SocketClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;


public class LBSocketClient extends SocketClient  {

    private final LoadBalance loadBalances;
    private final JSONObject errorMessage;

    LBSocketClient(Socket socket, LoadBalance lb) throws IOException {
        super(socket);
        loadBalances = lb;
        errorMessage = new JSONObject(new HashMap<String,String>(){
            { put("method","ERROR"); put("content","Internal Server Error, Retry"); }
        });
    }

    @Override
    protected void decode(String input) {
        JSONParser jsonParser = new JSONParser();

        HashMap<String,String> response = new HashMap<>();
        response.put("method","RESPONSE");

        try {
            JSONObject obj = (JSONObject) jsonParser.parse(input);
            
            switch (obj.get("method").toString()) {
                case "CONNECT" :
                    response.put("resource","ip");
                    response.put("content",String.valueOf(loadBalances.getReplica()));
                    break;
                case "CHECK" :
                    response.put("resource",obj.get("resource").toString());
                    response.put("content",Boolean.toString(loadBalances.checkStatus(obj.get("resource").toString())));
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

    @Override
    protected void close() {
        //Not implemented
    }
}
