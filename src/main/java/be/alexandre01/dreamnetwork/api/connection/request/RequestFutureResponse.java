package be.alexandre01.dreamnetwork.api.connection.request;


import be.alexandre01.dreamnetwork.client.connection.core.channels.ChannelPacket;

public interface RequestFutureResponse {
    void onReceived(ChannelPacket receivedPacket);


}