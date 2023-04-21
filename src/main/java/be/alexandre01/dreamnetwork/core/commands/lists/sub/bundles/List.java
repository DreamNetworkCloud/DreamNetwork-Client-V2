package be.alexandre01.dreamnetwork.core.commands.lists.sub.bundles;

import be.alexandre01.dreamnetwork.api.commands.sub.NodeBuilder;
import be.alexandre01.dreamnetwork.api.commands.sub.SubCommand;
import be.alexandre01.dreamnetwork.core.Main;
import be.alexandre01.dreamnetwork.core.console.Console;
import be.alexandre01.dreamnetwork.core.service.bundle.BundleData;
import lombok.NonNull;

import static be.alexandre01.dreamnetwork.api.commands.sub.NodeBuilder.create;

public class List extends SubCommand {
    public List(){
        NodeBuilder nodeBuilder = new NodeBuilder(
                create("bundle",
                    create("list")));
    }
    @Override
    public boolean onSubCommand(@NonNull String[] args) {
        boolean b = when(sArgs -> {
            for (BundleData bundleData : Main.getInstance().getBundleManager().getBundleDatas().values()){
                Console.printLang("commands.bundle.list.bundleName", bundleData.getBundleInfo().getName());
                Console.printLang("commands.bundle.list.bundleType", bundleData.getBundleInfo().getType());
                Console.printLang("commands.bundle.list.bundleExecutors", bundleData.getExecutors());
            }
            return true;
        },args,"list");
        return b;
    }

}