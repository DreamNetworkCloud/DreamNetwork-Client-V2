package be.alexandre01.dreamnetwork.api.connection.core.communication;

import be.alexandre01.dreamnetwork.client.connection.core.communication.Client;
import io.netty.channel.ChannelHandlerContext;

public interface IClientManager {
    IClient registerClient(Client client);

    IClient getClient(String processName);

    IClient getClient(ChannelHandlerContext ctx);

    java.util.HashMap<String, IClient> getClients();

    java.util.HashMap<ChannelHandlerContext, IClient> getClientsByConnection();

    java.util.ArrayList<IClient> getDevTools();

    IClient getProxy();


}