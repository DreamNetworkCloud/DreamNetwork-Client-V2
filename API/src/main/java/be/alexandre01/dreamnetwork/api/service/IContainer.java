package be.alexandre01.dreamnetwork.api.service;


import be.alexandre01.dreamnetwork.api.service.bundle.BundleData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public interface IContainer {
    IJVMExecutor getJVMExecutor(String processName, BundleData bundleData);

    IJVMExecutor getJVMExecutor(String processName, String bundleName) throws NullPointerException;

    ArrayList<IJVMExecutor> getJVMExecutors();
    IJVMExecutor[] getJVMExecutorsFromName(String processName);

    Optional<IJVMExecutor> tryToGetJVMExecutor(String processName);

    Optional<IService> tryToGetService(String serviceName);

    Optional<IService> tryToGetService(String processName, int id);
    Collection<IJVMExecutor> getServersExecutors();
    Collection<IJVMExecutor> getProxiesExecutors();

    public enum JVMType {
        SERVER, PROXY
    }

    public IJVMExecutor initIfPossible(String pathName, String name, boolean updateFile, BundleData bundleData);

    public void stop(String name,String pathName);
}