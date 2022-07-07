package be.alexandre01.dreamnetwork.client.connection.core.channels;

import be.alexandre01.dreamnetwork.api.connection.core.channels.AChannelPacket;
import be.alexandre01.dreamnetwork.api.connection.core.communication.IClient;
import be.alexandre01.dreamnetwork.api.connection.request.RequestInfo;
import be.alexandre01.dreamnetwork.client.connection.core.communication.Client;
import be.alexandre01.dreamnetwork.api.connection.request.RequestBuilder;
import be.alexandre01.dreamnetwork.api.connection.request.RequestFutureResponse;
import be.alexandre01.dreamnetwork.client.utils.messages.Message;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

@SuppressWarnings("unused")
public class ChannelPacket extends AChannelPacket {
    private static int currentId;

    private RequestInfo requestInfo;
    private final GenericFutureListener<? extends Future<? super Void>> listener;
    private Integer RID = null;
    private final Message message;
    private final String provider;
    private RequestFutureResponse requestFutureResponse;

    private Client client;
    private final String channel;

    public ChannelPacket(Message message){
        this.message = message;
        if(message.hasRequest())
         this.requestInfo = message.getRequest();
        this.listener = null;
        if(message.containsKey("RID")){
            this.RID = message.getRequestID();
        }
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
    public void createResponse(Message message, IClient client){
        createResponse(message,client,null,"channel");
    }

    @Override
    public void createResponse(Message message, IClient client, String header){
        createResponse(message,client,null,header);
    }

    @Override
    public void createResponse(Message message, IClient client, GenericFutureListener<? extends Future<? super Void>> listener){
        createResponse(message,client,listener,"channel");
    }


    @Override
    public void createResponse(Message message, IClient client, GenericFutureListener<? extends Future<? super Void>> listener, String header){
        message.setProvider(provider);
        message.setSender("core");
        message.setHeader(header);
        message.setChannel(this.channel);

        if(requestInfo != null){
            RequestBuilder.RequestData requestData = client.getRequestManager().getRequestBuilder().getRequestData().get(requestInfo);
            if(requestData != null)
            message = requestData.write(message,client,this.provider);
        }


        if(RID != null)
            message.setInRoot("RID",RID);
        client.writeAndFlush(message,listener);
    }

}
