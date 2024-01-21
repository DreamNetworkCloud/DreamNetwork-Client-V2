package be.alexandre01.dreamnetwork.core.websocket.sessions.frames;

import be.alexandre01.dreamnetwork.api.console.Console;
import be.alexandre01.dreamnetwork.api.service.*;
import be.alexandre01.dreamnetwork.api.service.screen.IScreen;
import be.alexandre01.dreamnetwork.api.utils.messages.Message;
import be.alexandre01.dreamnetwork.api.utils.messages.WebMessage;
import be.alexandre01.dreamnetwork.core.Core;
import be.alexandre01.dreamnetwork.core.service.screen.ScreenManager;
import be.alexandre01.dreamnetwork.core.websocket.sessions.FrameAbstraction;
import be.alexandre01.dreamnetwork.core.websocket.sessions.WebSession;

public class ServicesFrame extends FrameAbstraction {
    IExecutor currentExecutor;

    ExecutorCallbacks.ICallbackStart startHandler = service -> {
        getSession().send(composeNewService(service));
    };

    ExecutorCallbacks.ICallbackStop stopHandler = service -> {
        getSession().send(new WebMessage()
                .put("instruction","remove")
                .put("name", service.getFullIndexedName())
        );
    };
    public ServicesFrame(WebSession session) {
        super(session, "services");
    }

    public WebMessage composeNewService(IService service){
        return new WebMessage()
                .put("name", service.getFullIndexedName())
                .put("type", service.getType().name())
                .put("players", "N/A")
                .put("xmx", service.getXmx())
                .put("xms", service.getXms())
                .put("startup", service.getUsedConfig().getStartup())
                .put("time",service.getElapsedTime())
                .put("instruction","add");
    }
    @Override
    public void handle(WebMessage webMessage) {
        System.out.println("Handling ServicesFrame : " + webMessage);
        if(webMessage.containsKey("executor")) {
            IExecutor executor = Core.getInstance().getJvmContainer().findExecutor(webMessage.getString("executor")).orElse(null);
            if(executor != null){
                if(webMessage.containsKey("instruction")){
                    if(webMessage.getString("instruction").equals("simpleStart")){
                        executor.startService();
                        return;
                    }
                    if(webMessage.getString("instruction").equals("start")){
                        int number = webMessage.getInt("number");
                        String profile = webMessage.getString("profile");
                        if(profile == "default"){
                            profile = null;
                        }
                        String xms = webMessage.getString("xms");
                        String xmx = webMessage.getString("xmx");
                        boolean isStatic = webMessage.getBoolean("isStatic");

                        IExecutor.Mods type = isStatic ? IExecutor.Mods.STATIC : IExecutor.Mods.DYNAMIC;
                        Integer port;
                        if(webMessage.containsKey("port")){
                            port = webMessage.getInt("port");
                            String startup = webMessage.getString("startup");
                            try {
                                System.out.println("HOLA !");
                                System.out.println("Number : " + number);
                                System.out.println("Xms : " + xms);
                                System.out.println("Xmx : " + xmx);
                                System.out.println("Port : " + port);
                                System.out.println("Startup : " + startup);
                                System.out.println("Type : " + type);
                                executor.startServices(number,IStartupConfig.builder().xms(xms).xmx(xmx).port(port).startup(startup).type(type).buildFrom((IStartupConfig) executor));
                            }catch (Exception e){
                                System.out.println("HOLA");
                                Console.bug(e);
                            }

                            return;
                        }
                    }
                }
                currentExecutor = executor;
                for (IService service : executor.getServices()){
                    getSession().send(composeNewService(service));
                }
                executor.onNewServiceStart(startHandler);
                executor.onServiceStop(stopHandler);
        }
        }
        if(webMessage.containsKey("service")){
            IService service = Core.getInstance().getJvmContainer().findService(webMessage.getString("service")).orElse(null);
            if(service != null){
                if(webMessage.containsKey("instruction")){
                    switch (webMessage.getString("instruction")){
                        case "stop":
                            service.stop().whenComplete((aBoolean, throwable) -> {
                                if(aBoolean){
                                    System.out.println("Stop succeed");
                                }else{
                                    System.out.println("Stop failed");
                                }
                            });
                            break;
                        case "restart":
                            service.restart();
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void onLeave() {
        System.out.println("Leave ServicesFrame");
        if(currentExecutor == null) return;
        currentExecutor.removeCallback(startHandler);
        currentExecutor.removeCallback(stopHandler);
        currentExecutor = null;
    }
}