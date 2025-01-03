package be.alexandre01.dreamnetwork.core.websocket.sessions;

import be.alexandre01.dreamnetwork.api.service.IExecutor;
import be.alexandre01.dreamnetwork.api.service.IService;
import be.alexandre01.dreamnetwork.api.utils.messages.Message;
import be.alexandre01.dreamnetwork.api.utils.messages.WebMessage;
import be.alexandre01.dreamnetwork.api.utils.optional.Facultative;
import be.alexandre01.dreamnetwork.core.Core;
import be.alexandre01.dreamnetwork.core.websocket.HttpServerHandler;
import be.alexandre01.dreamnetwork.core.websocket.WebSocketHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;

import java.util.HashSet;

/*
 ↬   Made by Alexandre01Dev 😎
 ↬   done on 11/12/2023 at 15:19
*/
@Getter
public class WebSession {
    private final ChannelHandlerContext channelHandlerContext;
    private final HttpServerHandler httpServerHandler;
    private final WebSocketHandler webSocketHandler;
    private final HashSet<CloseListener> closeListeners = new HashSet<>();
    private final HashSet<MessageListener> messageListeners = new HashSet<>();

    private final FrameManager frameManager = new FrameManager(this);

    public WebSession(ChannelHandlerContext ctx, HttpServerHandler httpServerHandler, WebSocketHandler webSocketHandler) {
        this.channelHandlerContext = ctx;
        this.httpServerHandler = httpServerHandler;
        this.webSocketHandler = webSocketHandler;
        WebSessionManager.getInstance().registerSession(this);
        onRead(new FrameListener(this));
        onRead(message -> {
            if(message.containsKey("request")){
                if(message.getString("request").equalsIgnoreCase("services")){
                    if(message.containsKey("find")){
                        String find = message.getString("find");
                        Facultative.ifPresentOrElse(Core.getInstance().getJvmContainer().findService(find),service -> {
                            WebMessage executorMsg = WebComposerUtils.composeExecutor(service.getExecutor());
                            executorMsg.put("response","find").put("rType","executor");
                            send(executorMsg);
                            WebMessage serviceMsg = WebComposerUtils.composeNewService(service);
                            serviceMsg.put("response","find").put("rType","service");
                            send(serviceMsg);
                        },() -> {
                            WebMessage webMessage = new WebMessage();
                            webMessage.put("response","find").put("rType","notFound");
                        });

                    }
                }

            }
        });
    }

    public void send(WebMessage message){
        //System.out.println("Send message : " + message.toString());
        //System.out.println("Channel : " + channelHandlerContext.channel());
        //System.out.println("Channel is open : " + channelHandlerContext.channel().isOpen());
        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(message.toString()));
    }

    public void onRead(MessageListener messageListener){
        messageListeners.add(messageListener);
    }

    public void onClose(CloseListener closeListener){
        closeListeners.add(closeListener);
    }

    public void close(){
        if(channelHandlerContext.channel().isOpen())
            channelHandlerContext.channel().close();

        WebSessionManager.getInstance().unregisterSession(this);
        getCloseListeners().forEach(CloseListener::onClose);
    }

    public interface MessageListener{
        void onRead(WebMessage message);
    }

    public interface CloseListener{
        void onClose();
    }
}
