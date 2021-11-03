package be.alexandre01.dreamnetwork.client.service.screen;

import be.alexandre01.dreamnetwork.client.Client;
import be.alexandre01.dreamnetwork.client.config.Config;
import be.alexandre01.dreamnetwork.client.connection.core.communication.ClientManager;
import be.alexandre01.dreamnetwork.client.console.Console;
import be.alexandre01.dreamnetwork.client.service.JVMExecutor;
import be.alexandre01.dreamnetwork.client.service.JVMService;
import be.alexandre01.dreamnetwork.client.service.screen.stream.ScreenStream;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
@EqualsAndHashCode(callSuper = true)
@Data
public class  Screen extends Thread {
    JVMService service;
    ArrayList<String> history;
    ArrayList<ClientManager.Client> devToolsReading = new ArrayList<>();
    ScreenStream screenStream;
    Integer screenId;
    String screenName;

    public Screen(JVMService service){
        this.service = service;
        this.history = new ArrayList<>();
        ScreenManager screenManager = ScreenManager.instance;
        screenId = screenManager.getId(service.getJvmExecutor().getName());
        screenName = service.getJvmExecutor().getName()+"-"+screenId;
        this.screenStream = new ScreenStream(screenName,this);
        screenManager.addScreen(this);
    }

    @Override
    public void run() {
    }


    public void destroy(){
        if(Console.actualConsole.equals("s:"+screenName)){
            Console.getConsole("s:"+screenName).destroy();
            Console.setActualConsole("m:default");
            System.out.println("Le PROCESSUS "+service.getJvmExecutor().getName()+" vient de se tuer.");

        }
        ScreenManager.instance.remScreen(this);
        screenStream.exit();

        if(getService().getJvmExecutor().getType() == JVMExecutor.Mods.DYNAMIC){
            String t = getService().getJvmExecutor().isProxy() ? "proxy" : "server";
            Config.removeDir("/temp/"+ t + "/"+ getService().getJvmExecutor().getName()+"/"+getService().getJvmExecutor().getName()+"-"+getService().getId());
        }
    }

    public JVMService getService() {
        return service;
    }

    public ArrayList<String> getHistory() {
        return history;
    }

    public ScreenStream getScreenStream() {
        return screenStream;
    }
}
