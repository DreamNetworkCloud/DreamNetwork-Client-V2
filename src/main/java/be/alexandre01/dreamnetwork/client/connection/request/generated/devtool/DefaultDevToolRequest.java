package be.alexandre01.dreamnetwork.client.connection.request.generated.devtool;

import be.alexandre01.dreamnetwork.client.connection.request.RequestBuilder;
import be.alexandre01.dreamnetwork.client.connection.request.RequestType;

import java.util.Arrays;

public class DefaultDevToolRequest extends RequestBuilder {
    public DefaultDevToolRequest() {
        requestData.put(RequestType.DEV_TOOLS_HANDSHAKE,(message, client, args) -> {
            message.set("STATUS","SUCCESS");
            return message;
        });
        requestData.put(RequestType.SPIGOT_EXECUTE_COMMAND,(message,client, args) -> {
            message.set("CMD", args[0]);
            return message;
        });
        requestData.put(RequestType.DEV_TOOLS_NEW_SERVER,(message, client, args) -> {
            message.set("SERVERS", Arrays.asList(args));
            return message;
        });
        requestData.put(RequestType.CORE_STOP_SERVER, ((message, client, args) -> {
            return message;
        }));

    }
}