package be.alexandre01.dreamnetwork.api.console;

import be.alexandre01.dreamnetwork.api.DNCoreAPI;
import be.alexandre01.dreamnetwork.api.DNUtils;
import be.alexandre01.dreamnetwork.api.config.Config;
import be.alexandre01.dreamnetwork.api.config.IConfigManager;
import be.alexandre01.dreamnetwork.api.console.colors.Colors;
import be.alexandre01.dreamnetwork.api.console.formatter.IFormatter;
import be.alexandre01.dreamnetwork.api.console.history.ReaderHistory;
import be.alexandre01.dreamnetwork.api.console.language.ILanguageManager;
import be.alexandre01.dreamnetwork.api.utils.buffers.FixedSizeRingBuffer;
import lombok.Getter;
import lombok.Setter;
import org.jline.builtins.Completers;
import org.jline.reader.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Console {
    @Getter @Setter private boolean showInput;

    public static Console MAIN;
    @Getter private final ArrayList<Overlay> overlays = new ArrayList<>();

    private static final ArrayList<String> previousConsole = new ArrayList<>();

    public interface IConsole{
        public void listener(String[] args);
        public void consoleChange();
    }



    public static String getEmoji(String emoji,String ifNot,String... args){
        return DNUtils.get().getConfigManager().getLanguageManager().getEmojiManager().getEmoji(emoji,ifNot,args);
    }
    public static String getEmoji(String emoji, String ifNot){
        return DNUtils.get().getConfigManager().getLanguageManager().getEmojiManager().getEmoji(emoji,ifNot);
    }

    public static CompletableFuture<String[]> getFutureInput(){
        CompletableFuture<String[]> completableFuture = new CompletableFuture<>();
        Console.getCurrent().addOverlay(new Overlay() {
            @Override
            public void on(String data) {
                completableFuture.complete(data.split(" "));
                disable();
            }
        });
        return completableFuture;
    }
    public static String getEmoji(String emoji){
        return getEmoji(emoji,"");
    }

    public static IFormatter getFormatter(){
        return DNUtils.get().getConsoleManager().getFormatter();
    }

    public static IConsoleReader getConsoleReader(){
        return DNUtils.get().getConsoleManager().getConsoleReader();
    }
    public void addOverlay(Overlay overlay,String writing){
        if(writing != null){
            overlay.oldWriting = this.writing;
            this.writing = writing;
        }
        overlay.console = this;
        overlays.add(overlay);
    }

    public void addOverlay(Overlay overlay){
        addOverlay(overlay,null);
    }

    public abstract static class Overlay{
        private Console console;
        private String oldWriting;
        public abstract void on(String data);

        public void disable(){
            console.overlays.remove(this);
            console.writing = oldWriting;
        }
    }

    private StringBuilder datas = new StringBuilder();


    public List<Completers.TreeCompleter.Node> completorNodes = new ArrayList<>();
    IConsole iConsole;
    @Getter private static final HashMap<String, Console> instances = new HashMap<>();
    @Getter public String name;


    @Getter @Setter private boolean noHistory = false;

    @Getter private static boolean blockConsole = false;

    public static void setBlockConsole(boolean blockConsole) {
        if(blockConsole == Console.blockConsole) return;
        Console.blockConsole = blockConsole;

        if(ConsoleThread.get() == null || blockConsole != ConsoleThread.get().isRunning) return; // si console bloqué (true) alors isRunning = false


        Thread thread = ConsoleThread.get();
        if(blockConsole){
        //    System.out.println("Stopping console");
            if(thread != null){
                ConsoleThread.stopCurrent();// tell the thread to stop
            }
        }else {
           // System.out.println("Rerun console");
            ConsoleThread.startCurrent();
        }
        //reload();
    }

    private final FixedSizeRingBuffer<ConsoleMessage> history;
    //private int historySize;
    public static String defaultConsole;
    public boolean isDebug = false;
    public static String actualConsole;
    private Thread thread;
    public boolean isRunning = false;
    public boolean collapseSpace = false;

    public Character readLineChar = null;
    public String readLineString1 = null;
    public String readLineString2 = null;
    public MaskingCallback maskingCallback = null;
    @Setter public String writing = Console.getFromLang("console.dreamnetworkWriting",DNUtils.get().getConfigManager().getUsername());
    public PrintStream defaultPrint;
    @Setter @Getter private ConsoleKillListener killListener = new ConsoleKillListener() {
        @Override
        public boolean onKill(LineReader reader) {
            String data;
            DNUtils utils = DNUtils.get();
            utils.getConsoleManager().getConsoleReader().getDefaultHighlighter().setEnabled(false);
            try {
                while ((data = reader.readLine(Console.getFromLang("console.askExit"))) != null){
                    if(data.equalsIgnoreCase("y") || data.equalsIgnoreCase("yes")){
                       // System.exit(0);
                        DNUtils.get().stop();
                        return false;
                    }else {
                        Console.debugPrint(Console.getFromLang("cancelled"));
                        //ConsoleThread.resetAndRun();
                        utils.getConsoleManager().getConsoleReader().getDefaultHighlighter().setEnabled(true);
                        return true;
                    }
                }
            }catch (Exception e){
                if(e instanceof ClassNotFoundException){
                    Console.print("Force close");
                    System.exit(0);
                }

                SIG_IGN();
                //ignore
                //Console.bug(e);
            }
            return false;
        }
    };

    public static Console load(String name){
        Console.fine("Loading console "+name);
        Console c = new Console(name);
        if(DNUtils.get().getConsoleManager().getFormatter() != null){
            c.defaultPrint = DNUtils.get().getConsoleManager().getFormatter().getDefaultStream();
        }
        instances.put(name,c);
        return c;
    }
    public static void setActualConsole(String name ,boolean isSilent, boolean clearConsole){
        Console console = instances.get(name);
        IConsoleReader consoleReader = DNUtils.get().getConsoleManager().getConsoleReader();
        consoleReader.getSReader().setPrompt(console.writing);
        String previous = actualConsole;

        Console.actualConsole = name;
        console.isRunning = true;
        if(clearConsole)
            clearConsole();

        if(console.defaultPrint != null && !isSilent)
            console.defaultPrint.println(Console.getFromLang("console.changed", console.getName()+"/"+ConsoleThread.instance)+Colors.RESET);
        if(!console.history.isEmpty()){
            if(console.defaultPrint != null){
                ConsoleMessage[] h = console.history.getTable();
                //  stashLine();
                for (ConsoleMessage s : h){
                    if(s.level == null){
                        consoleReader.getSReader().printAbove(s.content);
                       // console.defaultPrint.print(Colors.RESET+s.content+Colors.RESET);
                    }else{
                        console.forcePrint(s.content,s.level);
                    }
                }
            }
        }
        try {
            consoleReader.getSReader().getHistory().purge();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(!console.noHistory){
            if(ReaderHistory.getLines().containsKey(name)){
                List<String> h = new ArrayList<>(ReaderHistory.getLines().get(name));
                for (String s : h){
                    consoleReader.getSReader().getHistory().add(s);
                }

            }
        }
        if(actualConsole.equals(name)){
            consoleReader.getDefaultHighlighter().setEnabled(true);
        }else{
            consoleReader.getDefaultHighlighter().setEnabled(false);
        }

        console.iConsole.consoleChange();
        console.reloadCompletors();

        previousConsole.add(previous);
    }

    public static void setActualConsole(String name){
        setActualConsole(name,false,true);
    }


    public static void setActualConsole(String name,boolean isSilent){
        setActualConsole(name,isSilent,true);
    }

    public static void goToPrevious(){
        if(previousConsole.isEmpty()){
            Console.print("No previous console");
            return;
        }
        String previous = previousConsole.get(previousConsole.size()-1);
        previousConsole.remove(previousConsole.size()-1);
        setActualConsole(previous);
    }

    public void reloadCompletors(){
        IConsoleReader consoleReader = DNUtils.get().getConsoleManager().getConsoleReader();

        Console.fine("Reload completors of console");;

        if(completorNodes.isEmpty()){
            completorNodes.add(Completers.TreeCompleter.node(""));
        }
        consoleReader.setNodes(completorNodes);
        consoleReader.reloadCompleter();
    }
    public static void setDefaultConsole(String defaultConsole) {
        Console.defaultConsole = defaultConsole;
    }





    public static Console getConsole(String name) {
        return instances.get(name);
    }

    public static Console getCurrent(){
        return instances.get(actualConsole);
    }

    public static Collection<Console> getConsoles(){
        return instances.values();
    }

    public static void print(Object s, Level level){

        if(!instances.containsKey("m:default")){
            LineReader lineReader = DNUtils.get().getConsoleManager().getConsoleReader().getSReader();
            if(s == null)
                s = "null";
            //String msg = s.toString().replaceAll("\\s+$", "");

            lineReader.printAbove(s.toString());
            return;
        }
        if(level == Level.FINE){
            FileHandler fh = DNUtils.get().getConsoleManager().getFileHandler();
            String msg = s == null ? "null": s.toString();
            if(DNUtils.get().getConfigManager().isDebug()){
                instances.get(actualConsole).fPrint(s+ Colors.ANSI_RESET,level);
                // print(msg,Level.FINE);
                return;
            }
            if(fh != null){
                sendToLog(msg,Level.FINE,"global");
            }
            return;
        }

        instances.get(actualConsole).fPrint(s + Colors.ANSI_RESET,level,false);
        Console.getConsole(ConsolePath.Main.DEFAULT).refreshHistory(s + Colors.ANSI_RESET,level);
        //instances.get("m:default").fPrint(s+Colors.ANSI_RESET(),level);

    }

    public static void fine(Object s){
        print(s,Level.FINE);
    }
    public static void fineLang(String map, Object s){
        printLang(map,s,Level.FINE);
    }
    public static void print(String s, Level level,String name){

        instances.get(name).fPrint(s + Colors.ANSI_RESET,level);
    }
    public void forcePrint(String s, Level level){
        IConsoleManager consoleManager = DNUtils.get().getConsoleManager();
        if(Console.actualConsole.equals(name))
            consoleManager.getConsoleReader().getSReader().printAbove(consoleManager.getFormatter().getDefaultFormatter().format(new LogRecord(level, s.toString()+Colors.RESET)));
    }
    /*
    Print without log
     */
    public void printNL(Object s){
        if(Console.actualConsole.equals(name)){
            //  stashLine();
            getConsoleReader().getSReader().printAbove(s.toString());
            // defaultPrint.print(s);

            //  ConsoleReader.sReader.setPrompt(writing);
            // unstashLine();
        }
        refreshHistory(s + "");
    }

    /*
    Basic Print in console
     */

    public void fPrintLang(String map, Level level, Object... params){
        DNUtils utils = DNUtils.get();
        ILanguageManager langManager = utils.getConfigManager().getLanguageManager();
        if (utils.getConfigManager().isDebug()){
            fPrint(ILanguageManager.getMessage(map,params) + " ["+map+"]", level);
            return;
        }
        fPrint(ILanguageManager.getMessage(map,params),level);
    }
    public void fPrintLang(String map, Object... params){
        fPrintLang(map,Level.INFO,params);
    }
    public void fPrint(Object s,Level level,boolean saveHistory){
        //stashLine();

        if(Console.actualConsole.equals(name)){

            //Client.getLogger().log(level,s+Colors.ANSI_RESET());


            if(!isDebug && level == Level.FINE)
                return;
            DNUtils utils = DNUtils.get();
            LineReader lineReader = utils.getConsoleManager().getConsoleReader().getSReader();
           // int cols = lineReader.getTerminal().getSize().getColumns();
            IFormatter formatter = utils.getConsoleManager().getFormatter();

            String msg = formatter.getDefaultFormatter().format(new LogRecord(level, (String) s));
            msg = msg.replaceAll("\\s+$", "");


            lineReader.printAbove(msg);

            // ConsoleReader.sReader.setPrompt(writing);
        }
        sendToLog(s,level);
        if(saveHistory){
            refreshHistory(s + Colors.ANSI_RESET,level);
        }
    }
    public void fPrint(Object s,Level level){
        fPrint(s,level,true);
    }



    public static void printLang(String map,Level level,Object... params){
        if(DNUtils.get().getConfigManager().isDebug()) {
            print(ILanguageManager.getMessage(map,params) + " ["+map+"]", level);
            return;
        }
        print(ILanguageManager.getMessage(map,params),level);
    }

    public static void printLang(String map,Object... params){
        printLang(map,Level.INFO,params);
    }

    public static String getFromLang(String map,Object... params){
        return ILanguageManager.getMessage(map,params);
    }
    public static void print(Object s){
        DNUtils utils = DNUtils.get();
        IFormatter formatter = DNUtils.get().getConsoleManager().getFormatter();
        String msg = formatter.getDefaultFormatter().format(new LogRecord(Level.INFO, s+Colors.ANSI_RESET));
        msg = msg.replaceAll("\\s+$", "");
        sendToLog(s,Level.INFO,"global");
        utils.getConsoleManager().getConsoleReader().getSReader().printAbove(msg);
    }

    public void sendToLog(Object s,Level level){
        final String msgWithoutColorCodes = s.toString().replaceAll("\u001B\\[[;\\d]*m", "");
        DNUtils.get().getConsoleManager().getFileHandler().publish(new LogRecord(level, msgWithoutColorCodes + "| @" + name));
    }

    public static void sendToLog(Object s,Level level,String name){
        DNUtils utils = DNUtils.get();
        if(utils == null)
            return;
        if(utils.getConsoleManager().getFileHandler() == null)
            return;
        final String msgWithoutColorCodes = s.toString().replaceAll("\u001B\\[[;\\d]*m", "");
        DNUtils.get().getConsoleManager().getFileHandler().publish(new LogRecord(level, msgWithoutColorCodes + "| @" + name));
    }
    public static Logger getLogger(){
        return Logger.getGlobal();
    }
    public static void debugPrint(Object s){
        // stashLine();
        LineReader lineReader = DNUtils.get().getConsoleManager().getConsoleReader().getSReader();
        if(s == null)
            s = "null";
        //String msg = s.toString().replaceAll("\\s+$", "");

        lineReader.printAbove(s.toString());



        sendToLog(s,Level.FINE,"global");
        if(Console.MAIN != null){
            Console.MAIN.refreshHistory(s + "");
        }
        //lineReader.printAbove(s.toString());
        // Client.getInstance().formatter.getDefaultStream().println(s+Colors.ANSI_RESET());
        //unstashLine();
    }


    public static void clearConsole(){
        clearConsole(DNUtils.get().getConsoleManager().getFormatter().getDefaultStream());
    }
    public static void clearConsole(PrintStream printStream){
       //if(true) return;
        try
        {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows"))
            {
                new ProcessBuilder("cmd","/c","cls").inheritIO().start().waitFor();
            }
            else
            {
                printStream.print("\033[H\033[2J");
                printStream.flush();
            }
        }
        catch (final Exception ignored)
        {

        }
    }
    public void setConsoleAction(IConsole iConsole){
        this.iConsole = iConsole;
       /* if(thread == null){
            thread = new Thread(this);
           // thread.start();
        }*/

    }
    public IConsole getConsoleAction(){
        return iConsole;
    }
    public Console(String name){
        int historySize = DNUtils.get().getConfigManager().getGlobalSettings().getHistorySize();
        this.history = new FixedSizeRingBuffer<ConsoleMessage>(new ConsoleMessage[historySize]);
        this.name = name;
        //a faire attention
        if(DNCoreAPI.getInstance() != null)
            this.isDebug = DNUtils.get().getConfigManager().isDebug();
    }

    public static void reload(){

        Thread thread = ConsoleThread.get();
        if(thread != null){
            thread.interrupt();
            Console.getConsole("m:default").isRunning = false;// tell the thread to stop
            try {
                thread.join(); // wait for the thread to stop
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Console.getConsole("m:default").isRunning = true;
            thread.start();
        }
    }

    public static void bug(Throwable e,boolean isSilent){
        if(Console.actualConsole == null){
            e.printStackTrace();
            return;
        }


        Console console = Console.getConsole(actualConsole);
        DNUtils utils = DNUtils.get();
        IConsoleManager consoleManager = utils.getConsoleManager();
        if(!isSilent){
            Console.printLang("console.errorOn", Level.WARNING, e.getMessage(), e.getClass().getSimpleName());
        }else {
            consoleManager.getFileHandler().publish(new LogRecord(Level.WARNING,Console.getFromLang("console.errorOn", e.getMessage(), e.getClass().getSimpleName())));
        }
        if(!isSilent){
            console.fPrintLang("console.errorCause", Level.SEVERE, e.getLocalizedMessage(), e.getClass().getSimpleName());
            for(StackTraceElement s : e.getStackTrace()){
                //Core.getInstance().formatter.getDefaultStream().println("----->");
                console.fPrintLang("console.errorOn", Level.SEVERE, s.getClassName(), s.getMethodName()+" on "+s.getFileName(), s.getLineNumber());
            }
        }else{
            consoleManager.getFileHandler().publish(new LogRecord( Level.SEVERE,Console.getFromLang("console.errorCause", e.getLocalizedMessage(), e.getClass().getSimpleName())));
            for(StackTraceElement s : e.getStackTrace()){
                //Core.getInstance().formatter.getDefaultStream().println("----->");
                consoleManager.getFileHandler().publish(new LogRecord(Level.SEVERE,Console.getFromLang("console.errorOn", s.getClassName(), s.getMethodName()+" on "+s.getFileName(), s.getLineNumber())));
            }
        }



        if(utils.getConfigManager().isDebug()){
            e.printStackTrace(consoleManager.getFormatter().getDefaultStream());
        }else {
            if(!isSilent)
                consoleManager.getFormatter().getDefaultStream().println(Console.getFromLang("console.contactDNDevError"));
            consoleManager.getFileHandler().publish(new LogRecord(Level.SEVERE, Console.getFromLang("console.contactDNDevError")));
        }
    }
    public static void bug(Throwable e){
        bug(e,false);
    }
    public static void SIG_IGN(){
        if(!DNUtils.get().getConfigManager().getGlobalSettings().isSIG_IGN_Handler()){
            Console.debugPrint(Console.getFromLang("console.closed"));
            //System.exit(0);
            DNUtils.get().stop();
        }

        try {
            Class.forName("org.jline.reader.Macro");
        }catch (ClassNotFoundException e) {
            // handle on debug when compile another
            System.exit(0);
        }
        LineReader reader =  DNUtils.get().getConsoleManager().getConsoleReader().getSReader();


        //  reader.setPrompt( Colors.YELLOW+"enter the secret-code > "+Colors.RESET);
        // PrintWriter out = new PrintWriter(reader.getTerminal().writer());

        try {
           Console.getConsole(actualConsole).killListener.onKill(reader); // boolean
               // ConsoleThread.resetAndRun();
        }catch (UserInterruptException e){
            SIG_IGN();
        }
        catch (EndOfFileException e){
            SIG_IGN();
        }

    }
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();

      /*  while (readByte>-1 && readByte!= '\n')
        {
            sb.append((char) readByte);
            readByte = consoleReader.reader.read();
        }*/
        return IConsoleReader.getReader().readLine();
    }
    public FixedSizeRingBuffer<ConsoleMessage> getHistory() {
        return history;
    }
    public void refreshHistory(String data,Level lvl){
        IConfigManager configManager = DNUtils.get().getConfigManager();
        if(lvl.equals(Level.FINE) && !DNUtils.get().getConfigManager().isDebug()){
            return;
        }
        //debugPrint("history >> "+data);

        history.fill(new ConsoleMessage(data,lvl));
       // historySize += data.length();
    }
    public void refreshHistory(String data){
        IConfigManager configManager = DNUtils.get().getConfigManager();
        // debugPrint("history >> "+data);
        history.fill(new ConsoleMessage(data));
      //  historySize += data.length();
    }
    public String readLineTimeout(BufferedReader reader, long timeout) throws TimeoutException, IOException {
        long start = System.currentTimeMillis();

        while (!reader.ready()) {
            if (System.currentTimeMillis() - start >= timeout)
                throw new TimeoutException();

            // optional delay between polling
            try { Thread.sleep(50); } catch (Exception ignore) {}
        }

        return reader.readLine(); // won't block since reader is ready
    }
    public void destroy(){
        history.clear();
        instances.remove(name);
    }

    public void write(String str){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if(isRunning){
                        DNUtils.get().getConsoleManager().getFormatter().getDefaultStream().write(stringToBytesASCII(str));
                        scheduler.shutdown();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        },50,50, TimeUnit.MILLISECONDS);

    }
    public static byte[] stringToBytesASCII(String str) {
        char[] buffer = str.toCharArray();
        byte[] b = new byte[buffer.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) buffer[i];
        }
        return b;
    }


    private static Buffer stashed;

    public interface ConsoleKillListener {
        boolean onKill(LineReader reader);
    }
}