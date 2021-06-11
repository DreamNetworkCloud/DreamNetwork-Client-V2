package be.alexandre01.dreamnetwork.client.connection.core.communication;

import be.alexandre01.dreamnetwork.client.Client;
import be.alexandre01.dreamnetwork.client.utils.messages.Message;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

public class BaseResponse extends CoreResponse {

    @Override
    public void onResponse(Message message, ChannelHandlerContext ctx) throws Exception {
        if(message.contains("Bonjour")){
            System.out.println("Message reçu du client => "+ message.getString("Bonjour"));
        }
    }
}
