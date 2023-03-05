package be.alexandre01.dreamnetwork.core.commands.lists;


import be.alexandre01.dreamnetwork.api.commands.Command;
import be.alexandre01.dreamnetwork.api.commands.sub.NodeBuilder;
import be.alexandre01.dreamnetwork.core.console.Console;


import static org.jline.builtins.Completers.TreeCompleter.node;

public class SpigetCommand extends Command {

    public SpigetCommand(String name) {
        super(name);
        NodeBuilder nodeBuilder = new NodeBuilder(NodeBuilder.create("spiget"));
        //setCompletion(node("spiget"));
        //setCompletions(new StringsCompleter("spiget"));

        commandExecutor = new CommandExecutor() {
            @Override
            public boolean execute(String[] args) {
                Console.setActualConsole("m:spiget");
                return true;
            }
        };
    }


}