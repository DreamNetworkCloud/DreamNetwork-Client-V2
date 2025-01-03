package be.alexandre01.dreamnetwork.core.commands.lists;

import be.alexandre01.dreamnetwork.api.commands.Command;
import be.alexandre01.dreamnetwork.api.commands.sub.NodeBuilder;
import be.alexandre01.dreamnetwork.api.console.Console;
import be.alexandre01.dreamnetwork.api.console.colors.Colors;
import be.alexandre01.dreamnetwork.core.Main;

import static be.alexandre01.dreamnetwork.api.console.jline.completors.CustomTreeCompleter.node;


public class QuitCommand extends Command {
    public QuitCommand(String quit) {
        super(quit);
        NodeBuilder nodeBuilder = new NodeBuilder(NodeBuilder.create(NodeBuilder.of("quit",getBaseColor()+"quit"+Colors.RESET+" "+ Console.getEmoji("door"))));
        //setCompletion(node("quit"));
        //setCompletions(new StringsCompleter("quit"));

        commandExecutor = new CommandExecutor() {
            @Override
            public boolean execute(String[] args) {
                Main.stop();
                return true;
            }
        };
    }
    @Override
    public String getBaseColor() {
        return Colors.RED_UNDERLINED;
    }

    @Override
    public String getEmoji() {
        return Console.getEmoji("door");
    }
}
