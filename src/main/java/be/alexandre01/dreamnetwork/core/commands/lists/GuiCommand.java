package be.alexandre01.dreamnetwork.core.commands.lists;

import be.alexandre01.dreamnetwork.api.commands.Command;
import be.alexandre01.dreamnetwork.core.commands.lists.sub.gui.Intro;
import be.alexandre01.dreamnetwork.core.commands.lists.sub.gui.Stats;
import be.alexandre01.dreamnetwork.core.console.language.LanguageManager;


public class GuiCommand extends Command {

    public GuiCommand(String gui) {
        super(gui);
        addSubCommand("stats",new Stats());
        addSubCommand("intro",new Intro());


        getHelpBuilder().setTitleUsage(LanguageManager.getMessage("commands.gui.titleUsage"));
        getHelpBuilder().setCmdUsage("stats");
    }
}
