package it.polimi.distributedsystems.replica;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class RepSocketClientTest {

    @Test
    public void testJsonSImple() {
        JSONObject errorMessage = new JSONObject(new HashMap<String,String>(){{ put("method","ERROR"); put("content","Internal Server Error, Retry"); }});
        System.out.println(errorMessage.toJSONString());

        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject obj = (JSONObject) jsonParser.parse(errorMessage.toJSONString());
            System.out.println(obj.get("method"));
        } catch (ParseException e) {
            System.out.println("FAILED TO READ INCOMING JSON, send a NACK to client");
        }
    }
}