package be.alexandre01.dreamnetwork.core.service;

import be.alexandre01.dreamnetwork.api.connection.core.communication.IClient;
import be.alexandre01.dreamnetwork.api.service.IJVMExecutor;
import be.alexandre01.dreamnetwork.api.service.screen.IScreen;
import be.alexandre01.dreamnetwork.core.connection.core.communication.Client;
import be.alexandre01.dreamnetwork.api.connection.request.RequestType;
import be.alexandre01.dreamnetwork.api.service.IService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class JVMService implements IService {
    private int id;
    private int port;
    private Client client;
    private JVMExecutor jvmExecutor;
    private Process process;

    private IJVMExecutor.Mods type;

    private String xmx;
    private String xms;

    private IScreen screen = null;



    @Override
    public synchronized void stop(){
        if(screen != null){
            screen.destroy();
        }


        if(client != null){
            client.getRequestManager().sendRequest(RequestType.CORE_STOP_SERVER);
            client.getChannelHandlerContext().close();
        }else{
            process.destroy();
        }
    }




    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void kill() {
        process.destroy();
        process.destroyForcibly();
    }

    @Override
    public void restart(){
        if(screen != null){
            screen.destroy();
        }


        if(client != null){
            client.getRequestManager().sendRequest(RequestType.CORE_STOP_SERVER);
            client.getChannelHandlerContext().close();
        }else{
            process.destroy();
        }
    }

    @Override
    public synchronized void removeService() {
        jvmExecutor.removeService(this);
    }

    @Override
    public void setClient(IClient client) {
        this.client = (Client) client;
    }
}
