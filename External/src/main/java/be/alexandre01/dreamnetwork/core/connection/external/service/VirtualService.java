package be.alexandre01.dreamnetwork.core.connection.external.service;

import be.alexandre01.dreamnetwork.api.DNUtils;
import be.alexandre01.dreamnetwork.api.connection.core.communication.AServiceClient;
import be.alexandre01.dreamnetwork.api.connection.core.request.DNCallback;
import be.alexandre01.dreamnetwork.api.connection.core.request.RequestType;
import be.alexandre01.dreamnetwork.api.connection.core.request.TaskHandler;
import be.alexandre01.dreamnetwork.api.service.*;
import be.alexandre01.dreamnetwork.api.service.screen.IScreen;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class VirtualService implements IService {
    @Getter List<Runnable> stopsCallbacks = new ArrayList<>();
    @Getter long startTime = System.currentTimeMillis();
    @Setter int id = -1;
    @Setter int port = 0;
    AServiceClient client;
    VirtualExecutor virtualExecutor;

    @Setter ExecutorCallbacks executorCallbacks;

    @Getter @Setter IScreen screen;

    IConfig config = null;




    public VirtualService(AServiceClient client, VirtualExecutor executor){
        this.client = client;
        this.virtualExecutor = executor;
        config = IStartupConfigBuilder.builder()
                .exec(executor.getExecutable())
                .javaVersion(executor.getJavaVersion())
                .name(executor.getName())
                .pathName(executor.getPathName())
                .port(executor.getPort())
                .startup(executor.getStartup())
                .type(executor.getType())
                .xms(executor.getXms())
                .xmx(executor.getXmx())
                .build(false);
        config.setScreenEnabled(DNUtils.get().getConfigManager().getGlobalSettings().isExternalScreenViewing());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getIndexingId() {
        return 0;
    }

    @Override
    public String getFullIndexedName() {
        return null;
    }


    @Override
    public long getProcessID() {
        return -1;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Optional<String> getUniqueCharactersID() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getCustomName() {
        return Optional.empty();
    }

    @Override
    public String getXmx() {
        return "N/A";
    }

    @Override
    public String getXms() {
        return "N/A";
    }

    @Override
    public IExecutor.Mods getType() {
        return virtualExecutor.getType();
    }

    @Override
    public AServiceClient getClient() {
        return client;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public VirtualExecutor getExecutor() {
        return virtualExecutor;
    }

    @Override
    public String getFullName() {
        return virtualExecutor.getFullName()+ "-"+id;
    }

    @Override
    public String getFullName(boolean withBundlePath) {
        return virtualExecutor.getName()+"-"+id;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override // to optional
    public Process getProcess() {
        return null;
    }

    @Override
    public IScreen getScreen() {
        return screen;
    }

    @Override
    public IConfig getUsedConfig() {
        return config;
    }

    @Override
    public void setScreen(IScreen screen) {
        this.screen = screen;
    }

    @Override
    public CompletableFuture<Boolean> stop() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        DNCallback.single(client.getRequestManager().getRequest(RequestType.CORE_STOP_SERVER), new TaskHandler() {
            @Override
            public void onAccepted() {
                completableFuture.complete(true);
            }

            @Override
            public void onFailed() {
                completableFuture.complete(false);
            }
        }
        ).send();
        return completableFuture;
    }

    @Override
    public CompletableFuture<RestartResult> restart() {
        CompletableFuture<RestartResult> completableFuture = new CompletableFuture<>();
        stop().whenComplete((aBoolean, throwable) -> {
            if(aBoolean){
                ExecutorCallbacks c = getExecutor().startService();
                c.whenStart(new ExecutorCallbacks.ICallbackStart() {
                    @Override
                    public void whenStart(IService service) {
                        completableFuture.complete(new RestartResult(true,c));
                    }
                });
            }else {
                completableFuture.complete(new RestartResult(false,null));
            }
        });
        return completableFuture;
    }

    @Override
    public CompletableFuture<RestartResult> restart(IConfig config) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> kill() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        DNCallback.single(virtualExecutor.getExternalCore().getRequestManager().getRequest(RequestType.CORE_KILL_SERVER), new TaskHandler() {
                    @Override
                    public void onAccepted() {
                        completableFuture.complete(true);
                    }

                    @Override
                    public void onFailed() {
                        completableFuture.complete(false);
                    }
                }
        ).send();
        return completableFuture;
    }

    @Override
    public void removeService() {
        virtualExecutor.removeService(this);
    }

    @Override
    public void setClient(AServiceClient client) {
        this.client = client;
    }

    @Override
    public Optional<ExecutorCallbacks> getExecutorCallbacks() {
        return Optional.ofNullable(executorCallbacks);
    }


    public String getTrueFullName() {
        return virtualExecutor.getTrueFullName() + "-" + id;
    }
}
