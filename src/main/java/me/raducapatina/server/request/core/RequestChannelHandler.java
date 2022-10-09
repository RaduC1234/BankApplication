package me.raducapatina.server.request.core;

import me.raducapatina.server.request.AuthenticateRequest;
import me.raducapatina.server.request.UnknownRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RequestChannelHandler {

   /* List<Request> requests = new ArrayList<>(0);

    public void registerRequest(RequestChannelHandler.RequestType requestType) throws IllegalStateException {

    }

    public enum RequestType {

        AUTHENTICATE(new AuthenticateRequest()),

        UNKNOWN(new UnknownRequest());


        private Request instantiator;

        public Request getInstance() {
            return instantiator.get();
        }

        RequestType(Supplier<Request> instantiator) {
            this.instantiator = instantiator;
        }
    }

    public Request getNewRequest(RequestType type) {
        return type.getInstance();
    }*/
}
