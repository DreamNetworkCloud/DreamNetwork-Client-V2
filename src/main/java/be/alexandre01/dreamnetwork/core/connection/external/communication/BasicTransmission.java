package be.alexandre01.dreamnetwork.core.connection.external.communication;


import be.alexandre01.dreamnetwork.api.connection.core.channels.AChannelPacket;
import be.alexandre01.dreamnetwork.api.connection.core.channels.IDNChannel;
import be.alexandre01.dreamnetwork.api.connection.request.RequestInfo;
import be.alexandre01.dreamnetwork.api.connection.request.RequestType;
import be.alexandre01.dreamnetwork.core.Core;
import be.alexandre01.dreamnetwork.core.connection.core.channels.ChannelPacket;
import be.alexandre01.dreamnetwork.core.connection.external.ExternalCore;
import be.alexandre01.dreamnetwork.core.connection.external.requests.ExtResponse;
import be.alexandre01.dreamnetwork.core.utils.messages.Message;
import io.netty.channel.ChannelHandlerContext;


public class BasicTransmission extends ExtResponse {

    @Override
    public void onResponse(Message message, ChannelHandlerContext ctx) throws Exception {
        ChannelPacket receivedPacket = new ChannelPacket(message);
        if(message.getHeader().equals("channel")) {
        IDNChannel dnChannel = Core.getInstance().getChannelManager().getChannel(message.getChannel());
        if(dnChannel != null){
                if(!dnChannel.getDnChannelInterceptors().isEmpty()){
                    for (AChannelPacket.DNChannelInterceptor dnChannelInterceptor : dnChannel.getDnChannelInterceptors()){
                        dnChannelInterceptor.received(receivedPacket);
                    }
                }
                return;
            }
        }
        if(message.hasRequest()){
            RequestInfo requestInfo = message.getRequest();

            if(requestInfo.equals(RequestType.SERVER_HANDSHAKE_SUCCESS)) {
                final ExternalCore externalCore = ExternalCore.getInstance();
                String processName = message.getString("PROCESSNAME");
                // networkBaseAPI.setProcessName("s-" + processName);
              //  networkBaseAPI.setServerName(processName.split("-")[0]);
                try{
                    //networkBaseAPI.setID(Integer.parseInt(processName.split("-")[1]));
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("The connection has been established on the remote address: " + ctx.channel().remoteAddress());

               // NetworkBaseAPI.getInstance().callServerAttachedEvent();
            }  else if(requestInfo.equals(RequestType.PROXY_HANDSHAKE_SUCCESS)){
                String processName = message.getString("PROCESSNAME");
             //   final NetworkBaseAPI networkBaseAPI = NetworkBaseAPI.getInstance();
                //networkBaseAPI.setProcessName("p-"+processName);
               // networkBaseAPI.setServerName(processName.split("-")[0]);
                try{
                    //networkBaseAPI.setID(Integer.parseInt(processName.split("-")[1]));
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("The connection has been established on the remote address: "+ ctx.channel().remoteAddress());
            }
        }
    }
}