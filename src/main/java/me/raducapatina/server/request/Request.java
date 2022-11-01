package me.raducapatina.server.request;

import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class Request {

    public String requestType = "UNKNOWN"; // name of the request
    public boolean requestStatus = false; // if the request is received or sent 0 -> sent, 1 -> received
    public String requestContent = "{}"; // content of the request

    public abstract void onIncomingRequest(MessageChannel ctx, String message) throws JsonProcessingException;

    public abstract void sendRequest(MessageChannel ctx);

    public abstract void onAnswer(MessageChannel ctx, String string);

    public void onAllChannelContext(MessageChannel ctx, String message) {
    }

    public static String error(String requestType, int code) {
        return "{\"requestType\": \"" + requestType + "\", \"requestStatus\": 1, \"responseStats\" : 0, \"responseContent\" : {\"message\" : " + code +"}}";
    }

    public static String success(String requestType, int code) {
        return "{\"requestType\": \"" + requestType + "\", \"requestStatus\": 1, \"responseStats\" : 1, \"responseContent\" : {\"message\" : " + code +"}}";
    }

    public static String request(String requestType, String content) {
        return "{\"requestType\": \"AUTHENTICATION\", \"requestStatus\": 0, \"requestContent\" : {" + content + "}}";
    }
}
