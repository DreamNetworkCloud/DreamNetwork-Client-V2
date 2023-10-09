package be.alexandre01.dreamnetwork.api.service;

import be.alexandre01.dreamnetwork.api.connection.core.communication.AServiceClient;
import be.alexandre01.dreamnetwork.api.service.screen.IScreen;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IService {
    int getId();
    long getProcessID();
    int getPort();
    Optional<String> getUniqueCharactersID();
    String getXmx();
    String getXms();
    IJVMExecutor.Mods getType();
    AServiceClient getClient();
    boolean isConnected();
    IJVMExecutor getJvmExecutor();

    String getFullName();
    String getFullName(boolean withBundlePath);
    Process getProcess();
    IScreen getScreen();
    IConfig getUsedConfig();

    void setScreen(IScreen screen);
    CompletableFuture<Boolean> stop();
    CompletableFuture<Boolean> kill();
    CompletableFuture<RestartResult> restart();
    CompletableFuture<RestartResult> restart(IConfig config);

    void removeService();

    void setClient(AServiceClient client);
    Optional<ExecutorCallbacks> getExecutorCallbacks();


    @AllArgsConstructor
    static class RestartResult{
        @Getter private final boolean success;
        private final ExecutorCallbacks executorCallbacks;

        public Optional<ExecutorCallbacks> getExecutorCallbacks(){
            return Optional.ofNullable(executorCallbacks);
        }
    }
}
