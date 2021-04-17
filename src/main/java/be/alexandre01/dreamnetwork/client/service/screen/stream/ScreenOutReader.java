package be.alexandre01.dreamnetwork.client.service.screen.stream;


import be.alexandre01.dreamnetwork.client.Client;
import be.alexandre01.dreamnetwork.client.console.Console;
import be.alexandre01.dreamnetwork.client.console.colors.Colors;
import be.alexandre01.dreamnetwork.client.service.screen.Screen;
import be.alexandre01.dreamnetwork.client.service.screen.commands.ScreenCommands;
import be.alexandre01.dreamnetwork.client.service.screen.commands.ScreenExit;
import com.github.tomaslanger.chalk.Chalk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ScreenOutReader extends Thread{
    ScreenCommands commands;
    BufferedWriter writer;
    private String[] args;
    private final Screen screen;
    private final Console console;
    public boolean stop = false;
    public ScreenOutReader(Screen screen, Console console,BufferedWriter writer){
        console.fPrint("AHH",Level.INFO);
        Console.debugPrint("IFG0E0I");
        this.writer = writer;
        this.console = console;
        this.screen = screen;
        commands = new ScreenCommands(screen);
        /*reader = new BufferedReader(new InputStreamReader(screen.getScreenStream().in));
        write("> ");

        try {
            args = reader.readLine().split(" ");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        commands.addCommands(new ScreenExit(screen));


    }
    @Override
    public void run() {
        console.setConsoleAction(new Console.IConsole() {
            @Override
            public void listener(String[] args) {

                //   Console.debugPrint(String.valueOf(args.length));
                if (args.length != 0) {
                    //Console.debugPrint("capte");
                    boolean hasFound = false;
                    if (!args[0].equalsIgnoreCase(" ")) {
                        //Console.debugPrint(Arrays.toString(args));
                        if (!commands.check(args)) {
                            try {
                                //   Console.debugPrint("start");

                                //  Console.debugPrint("writer");

                                //writer.write(args[args.length-1]+"\n");
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] != null) {
                                        sb.append(args[i]);
                                    }
                                    if (args.length - 1 != i) {
                                        sb.append(" ");
                                    }

                                }
                                //   Console.debugPrint(sb.toString());
                                if (screen.getScreenStream().screenInReader.in != null) {
                                }

                                writer.write(sb.toString());
                                writer.newLine();
                                //  Console.debugPrint("write");
                                writer.flush();
                                // Console.debugPrint("flush");
                            } catch (IOException e) {
                                e.printStackTrace(Client.getInstance().formatter.getDefaultStream());
                            }
                        }
                    }
                }
            }

            @Override
            public void consoleChange() {

            }
        });



//            write("> ");
         /*   try {
                args = reader.readLine().split(" ");
            }catch (Exception e){

            }*/
        }

}