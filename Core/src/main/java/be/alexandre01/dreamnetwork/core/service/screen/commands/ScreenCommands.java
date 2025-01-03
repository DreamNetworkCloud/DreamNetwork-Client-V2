package be.alexandre01.dreamnetwork.core.service.screen.commands;


import be.alexandre01.dreamnetwork.api.commands.Command;
import be.alexandre01.dreamnetwork.api.service.screen.IScreen;

import java.util.ArrayList;

public class ScreenCommands {
    public ArrayList<Command.CommandExecutor> executorList;
    private IScreen screen;
    public ScreenCommands(IScreen screen){
        this.executorList = new ArrayList<>();
        this.screen = screen;
    }

    public void addCommands(Command.CommandExecutor executor){
        this.executorList.add(executor);
    }

    public boolean check(String[] args){
        boolean hasFound = false;
        for(Command.CommandExecutor executors : executorList){
            if(executors.execute(args)){
                hasFound = true;
            }
        }
        if(!hasFound){
            return false;
          //  screen;
        }
        return true;
    }
}
