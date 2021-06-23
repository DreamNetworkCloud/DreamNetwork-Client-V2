package be.alexandre01.dreamnetwork.client.connection.core.handler;

import be.alexandre01.dreamnetwork.client.Client;
import be.alexandre01.dreamnetwork.client.connection.core.communication.AuthentificationResponse;
import be.alexandre01.dreamnetwork.client.connection.core.communication.BaseResponse;
import be.alexandre01.dreamnetwork.client.connection.core.communication.ClientManager;
import be.alexandre01.dreamnetwork.client.connection.core.communication.CoreResponse;
import be.alexandre01.dreamnetwork.client.console.Console;
import be.alexandre01.dreamnetwork.client.utils.messages.Message;
import be.alexandre01.dreamnetwork.utils.Tuple;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CoreHandler extends ChannelInboundHandlerAdapter{

    private ArrayList<CoreResponse> responses = new ArrayList<>();
    //A PATCH
    private HashMap<Message, Tuple<Channel,GenericFutureListener<? extends Future<? super Void>>>> queue = new HashMap<>();
    private Client client;
    public CoreHandler(){
        this.client = Client.getInstance();
        this.client.setCoreHandler(this);
    }

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) {
        System.out.println("Wow une nouvelle connection");
        System.out.println("Local ADRESS " + ctx.channel().localAddress());
        System.out.println("Remote ADRESS " + ctx.channel().remoteAddress());
        responses.add(new BaseResponse());
        responses.add(new AuthentificationResponse());
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
        System.out.println("CHANNEL ACTIVE");
        System.out.println(ctx.channel().remoteAddress().toString().split(":")[0]);

        if(!queue.isEmpty()){
            taskQueue();
        }
    }

    private void taskQueue(){
        Message msg = (Message) queue.keySet().toArray()[0];
        Tuple<Channel,GenericFutureListener<? extends Future<? super Void>>> t = null;
        Channel channel;
        if(queue.containsKey(msg)){
            return;
        }
        t = queue.get(msg);
        channel = t.a();

        byte[] entry = msg.toString().getBytes(StandardCharsets.UTF_8);
        final ByteBuf buf = channel.alloc().buffer(entry.length);
        buf.writeBytes(entry);

        ChannelFuture future = channel.writeAndFlush(buf);
        future.addListener(f -> {
            queue.remove(msg);
            if(!queue.isEmpty()){
                taskQueue();
            }
        });
        if(t.b() != null){
            future.addListener(t.b());
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg; // (1)
        String s_to_decode = m.toString(StandardCharsets.UTF_8);
        //TO DECODE STRING IF ENCODED AS AES
        Message msgTest = new Message();
        msgTest.set("Hello","AIE AIE");
        byte[] entry = msgTest.toString().getBytes(StandardCharsets.UTF_8);
        final ByteBuf buf = ctx.alloc().buffer(entry.length);
        buf.writeBytes(entry);
        ctx.writeAndFlush(buf);
        Console.print("Channel READ " + msgTest);

        if(!Message.isJSONValid(s_to_decode))
            return;

        try {
            Message message = Message.createFromJsonString(s_to_decode);
            for(CoreResponse iBasicClientResponse : responses){
                try {
                    iBasicClientResponse.onResponse(message,ctx,client.getClientManager().getClient(ctx));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            m.release();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if(executorService != null){
            System.out.println("WOW");
            executorService.shutdown();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void writeAndFlush(Message msg, ClientManager.Client client){
        this.writeAndFlush(msg,null,client);
    }

    public void writeAndFlush(Message msg, GenericFutureListener<? extends Future<? super Void>> listener, ClientManager.Client client){
        System.out.println("write and flush>> "+ msg);
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        System.out.println(ctx);
        if(ctx == null || !ctx.channel().isActive() || !queue.isEmpty()){
            assert ctx != null;
            queue.put(msg,new Tuple<>(ctx.channel(),listener));
            return;
        }
        byte[] entry = msg.toString().getBytes(StandardCharsets.UTF_8);
        final ByteBuf buf = ctx.alloc().buffer(entry.length);
        buf.writeBytes(entry);
        if(listener == null){
            ctx.writeAndFlush(buf);
            return;
        }
        ctx.writeAndFlush(buf).addListener(listener);
    }
}