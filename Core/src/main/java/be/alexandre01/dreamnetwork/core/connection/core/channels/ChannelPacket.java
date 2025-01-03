package be.alexandre01.dreamnetwork.core.connection.core.channels;

import be.alexandre01.dreamnetwork.api.connection.core.channels.AChannelPacket;
import be.alexandre01.dreamnetwork.api.connection.core.communication.AServiceClient;
import be.alexandre01.dreamnetwork.api.connection.core.communication.UniversalConnection;
import be.alexandre01.dreamnetwork.api.connection.core.request.Packet;
import be.alexandre01.dreamnetwork.core.connection.core.communication.ServiceClient;
import be.alexandre01.dreamnetwork.api.connection.core.request.RequestBuilder;
import be.alexandre01.dreamnetwork.api.connection.core.request.RequestFutureResponse;
import be.alexandre01.dreamnetwork.api.connection.core.request.RequestInfo;
import be.alexandre01.dreamnetwork.api.utils.messages.Message;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;

import java.util.Collection;

@SuppressWarnings("unused")
public class ChannelPacket extends AChannelPacket implements Packet {
    private static int currentId;

    private RequestInfo requestInfo;
    private final GenericFutureListener<? extends Future<? super Void>> listener;

    @Getter private final Message message;
    @Getter private final String provider;
    private RequestFutureResponse requestFutureResponse;

    private ServiceClient client;
    private final String channel;

    public ChannelPacket(Message message){
        this.message = message;
        if(message.hasRequest())
         this.requestInfo = message.getRequest();
        this.listener = null;
        this.provider = message.getProvider();
        this.channel = message.getChannel();
    }
    public ChannelPacket(String channel,String provider){
        this.listener = null;
        this.provider = provider;
        this.channel = channel;
        this.message = null;
    }


    @Override
    public void createResponse(Message message, UniversalConnection client){
        createResponse(message,client,null,"channel");
    }

    @Override
    public void createResponse(Message message, UniversalConnection client, String header){
        createResponse(message,client,null,header);
    }

    @Override
    public void createResponse(Message message, UniversalConnection client, GenericFutureListener<? extends Future<? super Void>> listener){
        createResponse(message,client,listener,"channel");
    }


    @Override
    public void createResponse(Message message, UniversalConnection client, GenericFutureListener<? extends Future<? super Void>> listener, String header){
        message.setProvider(provider);
        message.setReceiver("core");
        message.setHeader(header);
        message.setChannel(this.channel);

        if(requestInfo != null){
            Collection<RequestBuilder.RequestData> requestData = client.getRequestManager().getRequestBuilder().getRequestData().get(requestInfo);
            if (requestData != null) {
                for (RequestBuilder.RequestData data : requestData) {
                    message = data.write(message, client, this.provider);
                }
            }
            if (RequestBuilder.getGlobalRequestData().containsKey(requestInfo)) {
                for (RequestBuilder.RequestData data : RequestBuilder.getGlobalRequestData().get(requestInfo)) {
                    message = data.write(message, client, this.provider);
                }
            }
        }
        client.writeAndFlush(message,listener);
    }

    @Override
    public AServiceClient getReceiver() {
        return client;
    }
}
