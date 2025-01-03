package be.alexandre01.dreamnetwork.api.utils;

import be.alexandre01.dreamnetwork.api.DNCoreAPI;
import be.alexandre01.dreamnetwork.api.console.Console;
import be.alexandre01.dreamnetwork.api.console.colors.Colors;

public class ASCIIART {
    public static void sendLogo(){

        //Chalk.setColorEnabled(true);
        Console.debugPrint(Colors.CYAN+
                         "                                  ./((((/.            \n" +
                        "                                (///////////          \n" +
                        "                       ,(((((*///////////////.        \n" +
                        "                       /(((((////////////////,        \n" +
                        "                   (((((((((////////"+ Colors.CYAN_BOLD_BRIGHT+"/////////////,    \n" +
                        "                 /(((((((((////////////////////////   \n" +
                        "                 /((((((((/////////////////////////   \n" +
                        "                  ,((((((/////////////////////////    \n"+
                        "                         ````````````````  ");
    }

    public static void sendTitle(){
        Console.debugPrint(Colors.CYAN+
                "______                               _   _        _                          _    \n" +
                        "|  _  \\                             | \\ | |      | |                        | |   \n" +
                        "| | | | _ __  ___   __ _  _ __ ___  |  \\| |  ___ | |_ __      __ ___   _ __ | | __\n" +
                        "| | | || '__|/ _ \\ / _` || '_ ` _ \\ | . ` | "+Colors.CYAN_BRIGHT+"/ _ \\| __|\\ \\ /\\ / // _ \\ | '__|| |/ /\n" +
                        "| |/ / | |  |  __/| (_| || | | | | || |\\  ||  __/| |_  \\ V"+Colors.PURPLE_BOLD_BRIGHT+"  V /| (_) || |   |   <     "+Colors.WHITE_BOLD_UNDERLINED+"2023"+Colors.RESET+Colors.PURPLE_BOLD_BRIGHT+"\n" +
                        "|___/  |_|   \\___| \\__,_||_| |_| |_|\\_| \\_/ \\___| \\__|  \\_/\\_/  \\___/ |_|   |_|\\_\\      "+Colors.YELLOW_BRIGHT_UNDERLINE +"(BETA V2 - PRE 1.12)\n" +Colors.RESET);
    }

    public static void sendTutorial(Console console){
        console.printNL(Colors.WHITE_BOLD_BRIGHT+"====================================================================================================");
        console.printNL(Colors.WHITE+"  _______      _                _         _ \n" +
                " |__   __|    | |              (_)       | |\n" +
                "    | | _   _ | |_  ___   _ __  _   __ _ | |\n" +
                "    | || | | || __|/ _ \\ | '__|| | / _` || |\n" +
                "    | || |_| || |_| (_) || |   | || (_| || |\n" +
                "    |_| \\__,_| \\__|\\___/ |_|   |_| \\__,_||_|\n" +
                "                                            \n" );
        console.printNL(Colors.WHITE_BOLD_BRIGHT+"==================================================================================================== \n\n"+ Colors.RESET);
    }
    public static void sendAdd(Console console){
        console.printNL(Colors.WHITE_BOLD_BRIGHT+"====================================================================================================");
        console.printNL(Colors.WHITE+"               _      _ \n" +
                "     /\\       | |    | |\n" +
                "    /  \\    __| |  __| |\n" +
                "   / /\\ \\  / _` | / _` |\n" +
                "  / ____ \\| (_| || (_| |\n" +
                " /_/    \\_\\\\__,_| \\__,_|\n" +
                "                        \n");
        console.printNL(Colors.WHITE_BOLD_BRIGHT+"==================================================================================================== \n\n"+ Colors.RESET);
    }

}
