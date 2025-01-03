package be.alexandre01.dreamnetwork.core.connection.external.service;

import be.alexandre01.dreamnetwork.api.DNUtils;
import be.alexandre01.dreamnetwork.api.connection.core.request.DNCallback;
import be.alexandre01.dreamnetwork.api.connection.core.request.RequestType;
import be.alexandre01.dreamnetwork.api.connection.core.request.TaskHandler;
import be.alexandre01.dreamnetwork.api.connection.external.ExternalClient;
import be.alexandre01.dreamnetwork.api.console.Console;
import be.alexandre01.dreamnetwork.api.installer.enums.InstallationLinks;
import be.alexandre01.dreamnetwork.api.service.*;
import be.alexandre01.dreamnetwork.api.service.bundle.BundleData;
import be.alexandre01.dreamnetwork.api.service.enums.ExecType;
import be.alexandre01.dreamnetwork.api.service.screen.IScreen;
import be.alexandre01.dreamnetwork.api.utils.messages.Message;
import lombok.Getter;


import java.io.File;
import java.util.*;

public class VirtualExecutor  implements IExecutor {
    @Getter final ExecutorCallbacks globalCallbacks = new ExecutorCallbacks();
    ConfigData configData;
    BundleData bundleData;
    HashMap<Integer,IService> serviceList = new HashMap<>();

    @Getter
    ExternalClient externalCore;



    public VirtualExecutor(ConfigData configData, BundleData bundle, ExternalClient externalCore){
        this.configData = configData;
        this.bundleData = bundle;
        this.externalCore = externalCore;
    }


    public VirtualService createOrGetService(Integer id){
        if(serviceList.containsKey(id)){
            return (VirtualService) serviceList.get(id);
        }
        VirtualService virtualService = new VirtualService(null, this);
        serviceList.put(id,virtualService);
        return virtualService;
    }



    @Override
    public ExecutorCallbacks startService() {
        return startService(":this:", new ExecutorCallbacks());
    }

    @Override
    public ExecutorCallbacks startService(String profile) {
        return startService(profile, new ExecutorCallbacks());
    }

    @Override
    public ExecutorCallbacks startService(IConfig jvmConfig) {
        return startService(jvmConfig, new ExecutorCallbacks());
    }

    @Override
    public ExecutorCallbacks startService(String profile, ExecutorCallbacks callbacks) {
        VirtualService virtualService = new VirtualService( null, this);
        sendStartCallBack(externalCore, callbacks, virtualService, profile);
        return null;
    }

    @Override
    public ExecutorCallbacks startService(ExecutorCallbacks callbacks){
        return startService(":this:",callbacks);
    }

    @Override
    public ExecutorCallbacks startService(IConfig jvmConfig, ExecutorCallbacks callbacks) {
        VirtualService virtualService = new VirtualService( null, this);
        sendStartCallBack(externalCore, callbacks, virtualService, jvmConfig);
        return callbacks;
    }

    private void sendStartCallBack(ExternalClient client, ExecutorCallbacks callbacks, VirtualService virtualService, Object o){
        System.out.println("Sending start callback to "+ getTrueFullName());
        DNCallback.multiple(client.getRequestManager().getRequest(RequestType.CORE_START_SERVER, getTrueFullName(),o), new TaskHandler() {
            Integer id;
            @Override
            public void onCallback() {
                if (hasType(TaskType.CUSTOM)) {
                    String custom = getCustomType();
                    if (custom.equalsIgnoreCase("STARTED")) {
                        Message message = getResponse();
                        if(message.contains("name")){
                            String name = message.getString("name");
                            String splittedName = name.split("-")[1];
                            if(splittedName.matches("[0-9]+")){
                                id = Integer.parseInt(splittedName);
                                serviceList.put(id,virtualService);
                                virtualService.setId(id);
                                virtualService.setExecutorCallbacks(callbacks);
                                IScreen screen = DNUtils.get().createScreen(virtualService);
                                screen.setViewing(DNUtils.get().getConfigManager().getGlobalSettings().isExternalScreenViewing());
                                virtualService.setScreen(screen);
                            }
                        }
                        destroy();
                        Console.print("[EXTERNAL] => "+Console.getFromLang("service.executor.serverStartProcess", getFullName()));
                        if(callbacks != null){
                            if(callbacks.onStart != null)
                                callbacks.onStart.forEach(iCallbackStart -> iCallbackStart.whenStart(virtualService));
                        }

                        return;
                    }
                }
                if (hasType(TaskType.IGNORED) || hasType(TaskType.REJECTED) || hasType(TaskType.FAILED)) {
                    Console.print("[EXTERNAL] => "+Console.getFromLang("service.executor.couldNotStart", getFullName()));
                    super.destroy();
                    if(callbacks != null){
                        callbacks.setHasFailed(true);
                        if(callbacks.onFail != null){
                            callbacks.onFail.forEach(ExecutorCallbacks.ICallbackFail::whenFail);
                        }
                    }
                }
            }
        }).send();

        // link
    }

    @Override
    public ExecutorCallbacks startServices(int i) {
        return startServices(i, ":this:");
    }

    @Override
    public ExecutorCallbacks startServices(int i, IConfig jvmConfig) {
        ExecutorCallbacks callbacks = new ExecutorCallbacks();
        for (int j = 0; j < i; j++) {
            startService(jvmConfig,callbacks);
        }
        return callbacks;
    }

    @Override
    public ExecutorCallbacks startServices(int i, String profile) {
        ExecutorCallbacks callbacks = new ExecutorCallbacks();
        for (int j = 0; j < i; j++) {
            startService(profile,callbacks);
        }
        return callbacks;
    }

    @Override
    public void removeService(IService service) {
        serviceList.remove(service.getId());
        IExecutor.super.removeService(service);
    }

    @Override
    public IService getService(Integer i) {
        return serviceList.get(i);
    }

    @Override
    public Collection<IService> getServices() {
        return serviceList.values();
    }

    @Override
    public boolean isProxy() {
        return bundleData.getJvmType().equals(IContainer.JVMType.PROXY);
    }

    @Override
    public String getName() {
        return configData.getName();
    }

    @Override
    public boolean isConfig() {
        return true;
    }

    @Override
    public boolean isFixedData() {
        return false;
    }

    @Override  // to optional
    public File getFileRootDir() {
        return null;
    }

    @Override //to optional
    public IConfig getConfig() {
        return IStartupConfig.builder().buildFrom(configData);
    }

    @Override // to optional
    public IStartupConfig getStartupConfig() {
        return IStartupConfig.builder().buildFrom(configData);
    }

    @Override
    public Mods getType() {
        return configData.getType();
    }

    @Override
    public String getXms() {
        return configData.getXms();
    }

    @Override
    public String getStartup() {
        return configData.getStartup();
    }

    @Override
    public String getExecutable() {
        return configData.getExecutable();
    }

    @Override
    public Optional<String> getCustomName() {
        return configData.getCustomName();
    }


    @Override
    public String getXmx() {
        return configData.getXmx();
    }

    @Override // to optional
    public String getPathName() {
        return null;
    }

    @Override
    public String getJavaVersion() {
        return configData.getJavaVersion();
    }

    @Override
    public int getPort() {
        return configData.getPort();
    }

    @Override
    public boolean hasExecutable() {
        return true;
    }

    @Override
    public BundleData getBundleData() {
        return bundleData;
    }

    @Override
    public String getFullName() {
        return getBundleData().getName()+"/"+getName();
    }

    @Override
    public Optional<ExecType> getExecType() {
        if(!getInstallLink().isPresent()) return Optional.empty();
        try {
            return Optional.of(ExecType.valueOf(getInstallLink().get().getExecType().name()));
        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public Optional<InstallationLinks> getInstallLink() {
        try {
            return Optional.of(InstallationLinks.valueOf(configData.getInstallInfo()));
        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public Optional<IProfiles> getJvmProfiles() {
        return Optional.empty();
    }

    public String getTrueFullName(){
        if(getBundleData().getVirtualName().isPresent()){
            return getBundleData().getVirtualName().get() + "/" + getName();
        }
        return null;
    }


}
