package ru.gkpromtech.exhibition.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

import ru.gkpromtech.exhibition.utils.Callback;
import ru.gkpromtech.exhibition.utils.SharedData;

public class ServiceClient extends HttpClient {
    private final static ResponseProcessor<JsonNode> mProcessor = new ResponseProcessor<JsonNode>() {
        @Override
        public JsonNode process(InputStream in) throws Exception {
            return new ObjectMapper().readTree(in);
        }
    };

    public static void getJson(String path, Callback<JsonNode> callback) {
        get(SharedData.REST_SERVER_URL + path, callback, mProcessor);
    }

    public static void getJsonByUrl(String url, Callback<JsonNode> callback) {
        get(url, callback, mProcessor);
    }

    public static JsonNode getJson(String path) throws Exception {
        return mProcessor.process(get(SharedData.REST_SERVER_URL + path));
    }
}


