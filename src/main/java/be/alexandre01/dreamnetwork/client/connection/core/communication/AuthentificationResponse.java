package be.alexandre01.dreamnetwork.client.connection.core.communication;

import be.alexandre01.dreamnetwork.client.Client;
import be.alexandre01.dreamnetwork.client.connection.request.RequestType;
import be.alexandre01.dreamnetwork.client.console.Console;
import be.alexandre01.dreamnetwork.client.service.JVMContainer;
import be.alexandre01.dreamnetwork.client.utils.messages.Message;
import io.netty.channel.ChannelHandlerContext;

public class AuthentificationResponse extends CoreResponse{
    @Override
    public void onResponse(Message message, ChannelHandlerContext ctx) throws Exception {
        System.out.println("Requete entrente->");
        System.out.println(message);
        if(message.hasRequest()){
            RequestType requestType = RequestType.getByID(message.getRequest());
            Console.print("REQUETE : "+ requestType);
            switch (requestType){
                case CORE_HANDSHAKE:
                    Console.print("HANDSHAKE");
                    if(!message.contains("INFO") && !message.contains("PORT") && !message.contains("PASSWORD")){
                        ctx.channel().close();
                        return;
                    }

                    String info = message.getString("INFO");
                    int port = message.getInt("PORT");
                    String password = message.getString("PASSWORD");

                    Console.print("CREATE CLIENT");
                    ClientManager.Client client = Client.getInstance().getClientManager().registerClient(ClientManager.Client.builder()
                            .coreHandler(Client.getInstance().getCoreHandler())
                            .info(info)
                            .port(port)
                            .channel(ctx.channel())
                            .build());

                    if (client.getJvmType().equals(JVMContainer.JVMType.PROXY)) {
                        client.getRequestManager().sendRequest(RequestType.BUNGEECORD_HANDSHAKE_SUCCESS);
                    }
                    if (client.getJvmType().equals(JVMContainer.JVMType.SERVER)) {
                        client.getRequestManager().sendRequest(RequestType.SPIGOT_HANDSHAKE_SUCCESS);
                    }
                    break;
            }
        }
    }
}