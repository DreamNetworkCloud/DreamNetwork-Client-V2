package be.alexandre01.dreamnetwork.core.connection.request.generated.proxy;

import be.alexandre01.dreamnetwork.api.connection.request.RequestBuilder;
import be.alexandre01.dreamnetwork.api.connection.request.RequestType;
import be.alexandre01.dreamnetwork.core.console.Console;

import java.util.logging.Level;

public class DefaultBungeeRequest extends RequestBuilder {
    public DefaultBungeeRequest() {
        requestData.put(RequestType.BUNGEECORD_HANDSHAKE_SUCCESS,(message, client, args) -> {
            message.set("STATUS","SUCCESS");
            message.set("PROCESSNAME", client.getJvmService().getJvmExecutor().getName()+"-"+client.getJvmService().getId());
            return message;
        });
        requestData.put(RequestType.BUNGEECORD_REGISTER_SERVER,(message,client, args) -> {
            Console.print("REQUEST REGISTER SERVER", Level.FINE);
            message.set("PROCESSNAME",args[0]);
            message.set("REMOTEIP",args[1]);
            message.set("PORT",args[2]);
            message.set("MODS",args[3]);
            return message;
        });
        requestData.put(RequestType.BUNGEECORD_UNREGISTER_SERVER,(message,client, args) -> {
            System.out.println("REQUEST UNREGISTER SERVER");
            message.set("PROCESSNAME",args[0]);
            return message;
        });
        requestData.put(RequestType.BUNGEECORD_EXECUTE_COMMAND,(message,client, args) -> {
            message.set("CMD", args[0]);
            return message;
        });
        requestData.put(RequestType.CORE_STOP_SERVER, ((message, client, args) -> {
          return message;
        }));
        requestData.put(RequestType.CORE_REGISTER_CHANNEL,(message, client, args) -> {
            message.set("CHANNEL", args[0]);
            message.set("MAP",args[1]);
            return message;
        });
    }
}