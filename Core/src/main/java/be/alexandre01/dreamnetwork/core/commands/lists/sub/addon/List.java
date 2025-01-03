package be.alexandre01.dreamnetwork.core.commands.lists.sub.addon;

import be.alexandre01.dreamnetwork.api.commands.Command;
import be.alexandre01.dreamnetwork.api.commands.sub.NodeBuilder;
import be.alexandre01.dreamnetwork.api.commands.sub.SubCommand;
import be.alexandre01.dreamnetwork.api.console.Console;
import be.alexandre01.dreamnetwork.core.Main;
import be.alexandre01.dreamnetwork.api.addons.AddonDownloaderObject;
import lombok.NonNull;

import java.io.File;
import java.util.HashMap;

public class List extends SubCommand {
    private HashMap<String, AddonDownloaderObject> addons = null;

    public List(Command command) {
        super(command);
        if(Main.getCdnFiles().isInstanced()) {
            addons = Main.getCdnFiles().getAddons();
        }
        NodeBuilder nodeBuilder = new NodeBuilder(
                NodeBuilder.create(value, NodeBuilder.create("list", NodeBuilder.create("installed", "officials")))
        );
    }

    @Override
    public boolean onSubCommand(@NonNull String[] args) {
        return when(sArgs -> {
            if(sArgs.length < 2 || (!sArgs[1].equalsIgnoreCase("installed") && !sArgs[1].equalsIgnoreCase("officials"))){
                Console.printLang("commands.addon.list.invalidArguments");
                return true;
            }
            if(sArgs[1].equalsIgnoreCase("installed")){
                File addonsFolder = new File("addons");
                String[] addonsList = addonsFolder.list((dir, name) -> name.endsWith(".jar"));
                StringBuilder sb = new StringBuilder();
                assert addonsList != null;
                for(String a : addonsList){
                    sb.append(a.subSequence(0,a.length()-4)).append(", ");
                }
                Console.printLang("commands.addon.list.installedAddons");
                Console.print((addonsList.length != 0 ? sb.toString().subSequence(0, sb.length()-2) : Console.getFromLang("commands.addon.list.noAddonInstalled")));
            }else{
                if(addons == null && Main.getCdnFiles().isInstanced()){
                    addons = Main.getCdnFiles().getAddons();
                }
                if(addons == null || addons.size() == 0){
                    Console.getFromLang("commands.addon.list.noOfficialAddon");
                    return true;
                }
                StringBuilder sb = new StringBuilder();
                for(AddonDownloaderObject addon : addons.values()){
                    sb.append(Console.getFromLang("commands.addon.list.name")).append(addon.getName()).append("\n");
                    sb.append(Console.getFromLang("commands.addon.list.version")).append(addon.getVersion()).append("\n");
                    sb.append(Console.getFromLang("commands.addon.list.description")).append(addon.getDescription()).append("\n");
                    sb.append(Console.getFromLang("commands.addon.list.author")).append(addon.getAuthor()).append("\n");
                    sb.append(Console.getFromLang("commands.addon.list.repositoryLink")).append(addon.getGithub()).append("\n");
                    sb.append("   ------------------------------------------------------").append("\n");
                }
                Console.printLang("commands.addon.list.officialsAddons");
                Console.print(sb.toString());
            }
            return true;
        },args,"install","[" + Console.getFromLang("name") + "]");
    }
}
