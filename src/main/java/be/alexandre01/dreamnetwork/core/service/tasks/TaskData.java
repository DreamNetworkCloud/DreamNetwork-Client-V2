package be.alexandre01.dreamnetwork.core.service.tasks;

import be.alexandre01.dreamnetwork.api.service.IConfig;
import be.alexandre01.dreamnetwork.api.service.IJVMExecutor;
import be.alexandre01.dreamnetwork.api.service.IService;
import be.alexandre01.dreamnetwork.api.service.IStartupConfig;
import be.alexandre01.dreamnetwork.core.Core;
import be.alexandre01.dreamnetwork.core.Main;
import be.alexandre01.dreamnetwork.core.service.ExecutorCallbacks;
import be.alexandre01.dreamnetwork.core.service.JVMConfig;
import be.alexandre01.dreamnetwork.core.service.JVMExecutor;
import be.alexandre01.dreamnetwork.core.service.JVMStartupConfig;
import be.alexandre01.dreamnetwork.core.utils.files.yaml.Ignore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter @Getter
public class TaskData {
    public String name;
    public String service;
    public int count;
    public TaskType taskType;
    public String profile = null;

    @Ignore int actualCount = 0;
    @Ignore IJVMExecutor jvmExecutor;
    @Ignore IConfig iConfig;



    public static enum TaskType{
        ALWAYS_ON, ON_START,CUSTOM;
    }


    public void operate(){

        if(jvmExecutor == null){
            jvmExecutor = Core.getInstance().getJvmContainer().tryToGetJVMExecutor(service);
            if(jvmExecutor == null){
                System.out.println("Service "+service+" not found");
                return;
            }
        }
        if(iConfig == null) {
            if(profile != null){
                if(jvmExecutor.getProfiles().getProfiles().containsKey(profile)){
                    iConfig = jvmExecutor.getProfiles().getProfiles().get(profile);
                    System.out.println(iConfig.getType().name());
                }
                if(!(jvmExecutor instanceof IConfig)){
                    Core.getInstance().getGlobalTasks().tasks.remove(this);
                }
                iConfig = JVMStartupConfig.builder(iConfig).buildFrom((IStartupConfig) jvmExecutor);
                System.out.println(iConfig.getType().name());
            }else {
                if (jvmExecutor instanceof IConfig) {
                    iConfig = (IConfig) jvmExecutor;
                } else {
                    Core.getInstance().getGlobalTasks().tasks.remove(this);
                }
            }




        }

        if((count - actualCount) > 0){
            System.out.println("Starting "+(count - actualCount)+" "+service+" with profile "+profile);
            int toStart = count - actualCount;
            actualCount += toStart;
            jvmExecutor.startServers(toStart,iConfig).whenFail(new ExecutorCallbacks.ICallbackFail() {
                @Override
                public void whenFail() {
                    actualCount--;
                }
            }).whenStop(new ExecutorCallbacks.ICallbackStop() {
                @Override
                public void whenStop(IService service) {
                    actualCount--;
                }
            });
        }

    }
}
