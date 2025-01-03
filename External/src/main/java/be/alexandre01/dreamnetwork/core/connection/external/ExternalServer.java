package be.alexandre01.dreamnetwork.core.connection.external;

import be.alexandre01.dreamnetwork.api.console.Console;
import be.alexandre01.dreamnetwork.core.connection.external.handler.ExternalClientPipeline;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExternalServer extends Thread{
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    public int trying = 0;
    public String ip;




    public ExternalServer(String ip){
        this.ip = ip;
    }

    @Override
    public void run() {
        connect(ip);
    }


    public void connect(String ip){
        String[] splittedIp = ip.split(":");
        ExternalCore.getInstance().setInit(true);

        String host = splittedIp[0];
        int port = 14520;

        try {
            port = Integer.parseInt(splittedIp[1]);
        }catch (Exception e){
            System.out.println("Ip don't contain port numbers");
            return;
        }
        System.out.println("Attempt to connect to "+ host+":"+port +"#TRY_"+ trying);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ExternalClientPipeline(this));


            System.out.println("Connecting to "+host+":"+port);
            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)
            ExternalCore.getInstance().setConnected(true);
            System.out.println("Connected to "+host+":"+port);

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            System.out.println("Error while connecting to "+host+":"+port);
            Console.bug(e);
        } finally {
            if(!ExternalCore.getInstance().isConnected()){
                ExternalCore.getInstance().exitMode();
            }else {
                ExternalCore.getInstance().setConnected(false);
                System.out.println("Retrying to connect...");
                executorService.scheduleAtFixedRate(() -> {
                    System.out.println("...");
                    connect(ip);
                    executorService.shutdown();
                },5,5, TimeUnit.SECONDS);
                trying ++;

                workerGroup.shutdownGracefully();
                if(trying > 6){
                    ExternalCore.getInstance().exitMode();
                }
            }
        }
    }
}