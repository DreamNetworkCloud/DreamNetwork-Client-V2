package be.alexandre01.dreamnetwork.client;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.alexandre01.dreamnetwork.client.commands.CommandReader;
import be.alexandre01.dreamnetwork.client.console.ConsoleReader;
import com.github.tomaslanger.chalk.Chalk;

import be.alexandre01.dreamnetwork.client.api.DNAPI;
import be.alexandre01.dreamnetwork.client.config.Config;
import be.alexandre01.dreamnetwork.client.config.SecretFile;
import be.alexandre01.dreamnetwork.client.console.Console;
import be.alexandre01.dreamnetwork.client.console.colors.Colors;
import be.alexandre01.dreamnetwork.client.installer.SpigetConsole;
import be.alexandre01.dreamnetwork.client.libraries.DownloadLibraries;
import be.alexandre01.dreamnetwork.client.libraries.LoadLibraries;
import be.alexandre01.dreamnetwork.client.service.JVMContainer;
import be.alexandre01.dreamnetwork.client.service.JVMExecutor;
import be.alexandre01.dreamnetwork.client.service.JVMService;
import lombok.Getter;
import sun.misc.Unsafe;


public class Main {
    @Getter
    public static Client instance;
    @Getter
    private JVMContainer jvmContainer;
    @Getter
    private SpigetConsole spigetConsole;
    @Getter
    private static String username;
    @Getter
    private static boolean disabling = false;
    @Getter
    private static boolean license;
    @Getter
    private static CommandReader commandReader;



    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        disableWarning();
        System.setProperty("illegal-access", "permit");

        commandReader = new be.alexandre01.dreamnetwork.client.commands.CommandReader();
        ConsoleReader.init();

        Console.clearConsole(System.out);
        Config.createDir("data");
        Config.removeDir("temp");

        DNAPI dnapi = new DNAPI();
        PrintStream outputStream = System.out;

       

        //UTF8
        Chalk.setColorEnabled(true);
        System.out.println(Colors.RESET);
        Logger.getGlobal().setLevel(Level.WARNING);
        System.setProperty("file.encoding","UTF-8");
        Logger.getLogger("jdk.event.security").setLevel(Level.OFF);
        Logger.getLogger("io.netty.util.internal.logging.InternalLoggerFactory").setLevel(Level.OFF);
        Logger.getLogger("jdk.internal.event.EventHelper").setLevel(Level.OFF);
        String pathSlf4J = Client.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "log4j.properties";
     /*   if(!Config.isWindows()){
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.FINE);
            Logger.getLogger("").addHandler(ch);
            Logger.getLogger("").setLevel(Level.FINE);
        }*/


        if(Config.isWindows()){
            Client.setUsername(username = System.getProperty("user.name"));
        }else {
            try {
                Client.setUsername( username = InetAddress.getLocalHost().getHostName());

            } catch (UnknownHostException e) {
                Client.setUsername(username = System.getProperty("user.name"));
            };
        }
        boolean l = false;

            SecretFile secretFile = null;
            try {
                secretFile = new SecretFile();
                secretFile.init();

            } catch (IOException e) {
                e.printStackTrace();
            }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    disabling = true;
                    if(instance != null){
                        boolean isReady = false;
                        for(JVMExecutor jvmExecutor : instance.getJvmContainer().jvmExecutorsProxy.values()){
                            if(!jvmExecutor.jvmServices.isEmpty()){
                                for(JVMService service : jvmExecutor.getServices()){
                                    //service.kill();
                                }
                            }

                        }

                        for(JVMExecutor jvmExecutor : instance.getJvmContainer().jvmExecutorsServers.values()){
                            if(!jvmExecutor.jvmServices.isEmpty()){
                                for(JVMService service : jvmExecutor.getServices()){
                                    //service.kill();
                                }
                            }
                        }
                        isReady = true;
                        if(!Config.isWindows()){
                            String[] defSIGKILL = {"/bin/sh","-c","stty intr ^C </dev/tty"};
                            Runtime.getRuntime().exec(defSIGKILL);
                        }


                        outputStream.println("\n"+Chalk.on("DreamNetwork process shutdown, please wait..."+Colors.RESET).bgMagenta().bold().underline().white());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else {
                        outputStream.println("\n"+Chalk.on("DreamNetwork process shutdown, please wait..."+Colors.RESET).bgMagenta().bold().underline().white());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }

            }
        });

            if(!dnapi.hasValidLicense(secretFile.getUuid(),secretFile.getSecret())){
                System.out.println(Colors.RED+ "The license key is invalid!");
                secretFile.deleteSecretFile();
                System.exit(1);
                return;
            }
            System.out.println(Colors.PURPLE+"Successfully authenticated !\n"+Colors.RESET);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            loadClient();

    }

    public static void loadClient(){
        Console.load("m:default").isRunning = true;


        instance = new Client();
        Client.instance = instance;

       
        new TemplateLoading();
    }
    private static void disableWarning() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);

            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception e) {
            // ignore
        }
    }
}
