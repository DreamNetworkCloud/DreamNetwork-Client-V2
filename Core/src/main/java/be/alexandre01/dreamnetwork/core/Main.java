package be.alexandre01.dreamnetwork.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.alexandre01.dreamnetwork.api.DNUtils;
import be.alexandre01.dreamnetwork.api.addons.DreamExtension;
import be.alexandre01.dreamnetwork.api.console.Console;
import be.alexandre01.dreamnetwork.core.commands.CommandReader;
import be.alexandre01.dreamnetwork.api.service.IJVMExecutor;
import be.alexandre01.dreamnetwork.api.service.IService;
import be.alexandre01.dreamnetwork.core.config.FileCopyAsync;
import be.alexandre01.dreamnetwork.api.config.GlobalSettings;
import be.alexandre01.dreamnetwork.core.console.ConsoleReader;
import be.alexandre01.dreamnetwork.api.console.history.ReaderHistory;
import be.alexandre01.dreamnetwork.api.console.language.ColorsConverter;
import be.alexandre01.dreamnetwork.core.console.language.LanguageManager;
import be.alexandre01.dreamnetwork.api.console.process.ProcessHistory;
import be.alexandre01.dreamnetwork.core.service.bundle.BundleManager;
import be.alexandre01.dreamnetwork.core.service.deployment.DeployListLoader;
import be.alexandre01.dreamnetwork.core.service.deployment.DeployManager;

import be.alexandre01.dreamnetwork.api.utils.files.CDNFiles;

import be.alexandre01.dreamnetwork.api.utils.process.ProcessUtils;
import com.github.tomaslanger.chalk.Chalk;

import be.alexandre01.dreamnetwork.core.rest.DNAPI;
import be.alexandre01.dreamnetwork.api.config.Config;
import be.alexandre01.dreamnetwork.core.config.SecretFile;
import be.alexandre01.dreamnetwork.api.console.colors.Colors;
import be.alexandre01.dreamnetwork.core.service.JVMContainer;
import lombok.Getter;
import lombok.Setter;
import org.jline.builtins.Completers;

public class Main {
    public static Core instance;

    @Getter
    @Setter
    public static BundleManager bundleManager;
    @Getter
    @Setter
    public static DeployManager deployManager;
    @Getter private static GlobalSettings globalSettings;
    @Getter private static FileCopyAsync fileCopyAsync;
    @Getter private static ConsoleReader consoleReader;
    @Getter private static UtilsAPI utilsAPI;

    @Getter
    private JVMContainer jvmContainer;
    @Getter
    private static String username;
    @Getter
    private static boolean disabling = false;
    @Getter
    private static boolean license;
    @Getter
    private static CommandReader commandReader;

    @Getter @Setter private static BundlesLoading bundlesLoading;

    @Getter private static LanguageManager languageManager;

    @Getter private static CDNFiles cdnFiles;

    @Getter private static ProcessHistory processHistory;



    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("illegal-access", "permit");
        System.setProperty("file.encoding", "UTF-8");

        Logger.getLogger("org.yaml.snakeyaml").setLevel(Level.OFF);

        utilsAPI = new UtilsAPI();
        boolean dataCreated = Config.contains("data");
        if(!dataCreated)
            new File(Config.getPath("data")).mkdir();

        globalSettings = new GlobalSettings();

        globalSettings.loading();
        if(Main.getGlobalSettings().getUsername() == null){
            if(Config.isWindows()){
                Core.setUsername(username = System.getProperty("user.name"));
            }else {
                try {
                    Core.setUsername( username = InetAddress.getLocalHost().getHostName());
                } catch (UnknownHostException e) {
                    Core.setUsername(username = System.getProperty("user.name"));
                };
            }
            Main.getGlobalSettings().setUsername(username);
            Main.getGlobalSettings().save();
        }else {
            String line = Main.getGlobalSettings().getUsername();
            for(ColorsConverter color : ColorsConverter.values()){line = line.replace("%" + color.toString().toLowerCase() + "%", color.getColor());}
            Core.setUsername(line);
            username = line;
        }

        fileCopyAsync = new FileCopyAsync();
        languageManager = new LanguageManager();

        if(!languageManager.load()){
            // Fetch fail, can't use messages
        }

        commandReader = new CommandReader();
        consoleReader = new ConsoleReader();
        consoleReader.init();

        ReaderHistory readerHistory = new ReaderHistory();
        readerHistory.init();

        // Start language fetching

        if(!dataCreated){

            ArrayList<String> languages = new ArrayList<String>();
            Collections.addAll(languages, LanguageManager.getAvailableLanguages());
            for(String lang : languages){
                consoleReader.nodes.add(Completers.TreeCompleter.node(lang));
            }
            consoleReader.sReader.runMacro("en_EN");
            String data;
            consoleReader.reloadCompleter();

            while((data = consoleReader.sReader.readLine(Colors.WHITE_BOLD_BRIGHT+"What is your language ? "+Colors.YELLOW+languages +" "+Colors.CYAN_BOLD_BRIGHT+"> ")) != null){
                if(data.length() == 0) continue;
                String l = data.split(" ")[0];
                  if(languages.contains(l)){
                      Main.getGlobalSettings().setLanguage(l);
                      Main.getGlobalSettings().save();
                      languageManager.loadDifferentLanguage(l);
                      break;
                  }
                  Console.clearConsole(System.out);
            }

        }

        cdnFiles = new CDNFiles();
        cdnFiles.start();

        consoleReader.initHighlighter();
        Console.clearConsole(System.out);
        processHistory = new ProcessHistory();
        processHistory.init();
        while (Config.removeDir("runtimes")){
            try {
                System.out.println(Colors.YELLOW_BOLD_BRIGHT+"Deleting runtimes folder but something is blocking it...");
                System.out.println(Colors.YELLOW_BOLD_BRIGHT+"Please close all the process that are using the runtimes folder");
                System.out.println(Colors.YELLOW_BOLD_BRIGHT+"Retrying it...");
                Thread.sleep(500);
                Console.clearConsole(System.out);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

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
     //   String pathSlf4J = Core.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "log4j.properties";
     /*   if(!Config.isWindows()){
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.FINE);
            Logger.getLogger("").addHandler(ch);
            Logger.getLogger("").setLevel(Level.FINE);
        }*/





        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    disabling = true;
                    if(instance != null){
                        boolean isReady = false;
                        for(IJVMExecutor jvmExecutor : instance.getJvmContainer().jvmExecutors){
                            if(!jvmExecutor.getServices().isEmpty()){
                                ArrayList<Long> pIDs = new ArrayList<>();
                                for(IService service : jvmExecutor.getServices()){
                                    if(!service.isConnected()){
                                        service.kill();
                                    }
                                    if(service.getProcessID() != -1){
                                        if(!service.isConnected())
                                            ProcessUtils.killProcess(service.getProcessID());
                                        pIDs.add(service.getProcessID());
                                    }
                                }
                                getProcessHistory().getProcessHistoryIndex().put("TMPProcess", Base64.getEncoder().encodeToString(ProcessHistory.convert(pIDs).getBytes(StandardCharsets.UTF_8)));
                                getProcessHistory().getProcessHistoryIndex().refreshFile();
                            }

                        }


                        if(consoleReader.sReader != null){
                            for (String key : ReaderHistory.getLines().keySet()) {
                               /* History h = ConsoleReader.sReader.getHistory();
                                ArrayList<String> l = new ArrayList<>();
                                for (int j = 0; j < h.size()-1; j++) {
                                    l.add(h.get(j));
                                }*/
                                ArrayList<String> l = new ArrayList<>(ReaderHistory.getLines().get(key));

                                List<String> tail = l.subList(Math.max(l.size() - 15, 0), l.size());
                                readerHistory.getReaderHistoryIndex().put(key, Base64.getEncoder().encodeToString(ReaderHistory.convert(tail).getBytes(StandardCharsets.UTF_8)));
                                readerHistory.getReaderHistoryIndex().refreshFile();

                            }
                        }
                        isReady = true;

                        if(getGlobalSettings().isSIG_IGN_Handler()){
                            if(!Config.isWindows()){
                                String[] defSIGKILL = {"/bin/sh","-c","stty intr ^C </dev/tty"};
                                Runtime.getRuntime().exec(defSIGKILL);
                            }
                        }

                        Core.getInstance().getAddonsManager().getAddons().values().forEach(DreamExtension::stop);
                        outputStream.println(Console.getFromLang("main.shutdown"));
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else {
                        outputStream.println(Console.getFromLang("main.shutdown"));
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
        boolean l = false;
        String keys = System.getProperty("keys");

        SecretFile secretFile = null;
        try {
            secretFile = new SecretFile();
            if(keys == null ){
                secretFile.init();
            }else {
                secretFile.init(keys);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
     /*   if(!dnapi.hasValidLicense(secretFile.getUuid(),secretFile.getSecret())){
            outputStream.println(Console.getFromLang("main.invalidLicenseKey"));
            secretFile.deleteSecretFile();
            System.exit(1);
            return;
        }*/
        outputStream.println(Console.getFromLang("main.successAuth"));
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        loadClient();
    }

    public static void loadClient(){
        Console.MAIN = Console.load("m:default");
        Console.MAIN.isRunning = true;

        instance = Core.getInstance();
        instance.afterConstructor();

        //Client.instance = instance;

        Main.setBundleManager(new BundleManager());
        Main.setDeployManager(new DeployManager());
        new DeployListLoader();
        new BundlesLoading();
        Core.getInstance().init();

    }
}