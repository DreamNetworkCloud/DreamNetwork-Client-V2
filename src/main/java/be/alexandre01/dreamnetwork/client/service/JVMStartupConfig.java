package be.alexandre01.dreamnetwork.client.service;

import be.alexandre01.dreamnetwork.client.Config;
import be.alexandre01.dreamnetwork.client.console.Console;
import be.alexandre01.dreamnetwork.client.console.colors.Colors;
import lombok.Data;
import lombok.Getter;

import java.io.*;
import java.util.Date;
import java.util.logging.Level;
@Data
public class JVMStartupConfig {
    @Getter boolean isConfig;
    @Getter String name;
    @Getter JVMExecutor.Mods type;
    @Getter String xms;
    @Getter String startup = null;
    @Getter String exec = null;
    @Getter String xmx;
    @Getter String pathName;
    @Getter int port = 0;
    @Getter long confSize = 0;
    @Getter boolean proxy = false;
    @Getter boolean fixedData = false;
    @Getter File fileRootDir;



    public JVMStartupConfig( String pathName,String name, JVMExecutor.Mods type, String xms, String xmx, int port, boolean proxy,boolean updateFile){
        this.name = name;
        this.type = type;
        this.xms = xms;
        this.xmx = xmx;
        this.port = port;
        this.proxy = proxy;
        this.pathName = pathName;
        this.fixedData = true;
        if(proxy){
            exec = "BungeeCord.jar";
        }else {
            exec = "Spigot.jar";
        }

        if(updateFile){
            updateConfigFile(pathName,name,type,xms,xmx,port,proxy,null,null);
        }
        try {
            for (String line : Config.getGroupsLines(System.getProperty("user.dir")+"/template/"+pathName+"/"+name+"/network.yml")){
                if(line.startsWith("startup:")){
                    startup = line;
                    startup = startup.replace("startup:","");
                    while (startup.charAt(0) == ' '){
                        startup = startup.substring(1);
                    }
                    startup =  startup.replaceAll("%xms%",xms);

                    startup =  startup.replaceAll("%xmx%",xmx);
                }
                if(line.startsWith("executable:")){
                    exec = line;
                    exec = exec.replace("executable:","");
                    exec = exec+".jar";
                    while (exec.charAt(0) == ' '){
                        exec = exec.substring(1);
                    }
                    System.out.println(exec);
                }
            }
            fileRootDir = new File(System.getProperty("user.dir")+"/template/"+pathName+"/"+name+"/");
            isConfig = true;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public JVMStartupConfig(String pathName,String name){
        this.name = name;
        this.pathName = pathName;
        update();
    }

    public void update(){
        try {
            for (String line : Config.getGroupsLines(System.getProperty("user.dir")+"/template/"+pathName+"/"+name+"/network.yml")){
                if(line.startsWith("type:")){
                    this.type = JVMExecutor.Mods.valueOf(line.replace("type:","").replaceAll(" ",""));
                }
                if(line.startsWith("xms:")){
                    this.xms = line.replace("xms:","").replaceAll(" ","");
                }
                if(line.startsWith("xmx:")){
                    this.xmx = line.replace("xmx:","").replaceAll(" ","");
                }
                if(line.startsWith("port:")){
                    this.port = Integer.parseInt(line.replace("port:","").replaceAll(" ",""));
                }
                if(line.contains("proxy: true")){
                    this.proxy = true;
                }
                if(proxy){
                    exec = "BungeeCord.jar";
                }else {
                    exec = "Spigot.jar";
                }

                if(line.startsWith("startup:")){
                    startup = line;
                    startup = startup.replace("startup:","");
                    while (startup.charAt(0) == ' '){
                        startup = startup.substring(1);
                    }
                    startup =  startup.replaceAll("%xms%",xms);
                    startup = startup.replaceAll("%xmx%",xmx);
                }
                if(line.startsWith("executable:")){
                    exec = line;
                    exec = exec.replace("executable:","");
                    while (exec.charAt(0) == ' '){
                        exec = exec.substring(1);
                    }

                }
                isConfig = true;
            }
        }catch (IOException e){
            Console.print(Colors.ANSI_RED()+"Le serveur en question n'est pas encore configuré",Level.SEVERE);
            isConfig = false;
            return;
        }
    }
    public boolean changePort(String pathName , String finalname , int port, JVMExecutor.Mods type){
        String name = finalname.split("-")[0];
        String fileName;
        String checker;
        boolean proxy = false;
        if(pathName.contains("server")){
            fileName = "server.properties";
            checker = "server-port=";
        }else {
            proxy = true;
            fileName = "files/bungeecord/config.yml";
            checker = "host: 0.0.0.0:";
        }
        File properties;
        if(type.equals(JVMExecutor.Mods.DYNAMIC)){
            properties = new File(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+finalname+"/"+fileName));
        }else {
            properties = new File(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+fileName));
        }

        if(!properties.exists()){
            return false;
        }
        try {
            BufferedReader file;

            if(type.equals(JVMExecutor.Mods.DYNAMIC)){
                file = new BufferedReader( new FileReader(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+finalname+"/"+fileName)));

            }else {
                file = new BufferedReader(new FileReader(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+fileName)));
            }

            StringBuffer inputBuffer = new StringBuffer();
            String line;

            while ((line = file.readLine()) != null) {
                if(line.startsWith("server-port=")){
                    line = "server-port= "+ port;
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            file.close();

            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut;
            if(type.equals(JVMExecutor.Mods.DYNAMIC)){
                fileOut = new FileOutputStream(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+finalname+"/"+fileName));
            }else {
                fileOut = new FileOutputStream(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+fileName));
            }
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();
            return true;
        } catch (Exception e) {
            System.out.println("Problem reading file.");
            return false;
        }

    }

    public Integer getCurrentPort(String pathName, String finalname, JVMExecutor.Mods type){
        System.out.println(pathName);
        System.out.println(finalname);
        System.out.println(type);
        String fileName;
        String checker;
        boolean proxy = false;
        if(pathName.contains("server")){
            fileName = "server.properties";
            checker = "server-port=";
        }else {
            proxy = true;
            fileName = "config.yml";
            checker = "host: 0.0.0.0:";
        }
        String name = finalname.split("-")[0];
        File properties;
        if(type.equals(JVMExecutor.Mods.DYNAMIC)){
            properties = new File(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+finalname+"/"+fileName));
        }else {
            String path;
            if(proxy){
                path = "proxy";
            }else {
                path = "server";
            }
            //   System.out.println(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name));
            properties= new File(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name));
        }

        if(!properties.exists()){
            return null;
        }
        try {

            BufferedReader file;
            if(type.equals(JVMExecutor.Mods.DYNAMIC)){
                file = new BufferedReader( new FileReader(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+finalname+"/"+fileName)));
            }else {
                file = new BufferedReader( new FileReader(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+fileName)));
            }

            StringBuffer inputBuffer = new StringBuffer();

            String line;
            Integer port = null;

            while ((line = file.readLine()) != null) {

                if(line.contains(checker)){

                    String readline = line.replace(checker,"").replaceAll(" ","");
                    //  System.out.println(readline);
                    port = Integer.parseInt(readline);
                    //  System.out.println(port);

                }
                inputBuffer.append(line);

                inputBuffer.append('\n');
            }
            file.close();

            FileOutputStream fileOut;
            if(type.equals(JVMExecutor.Mods.DYNAMIC)){
                fileOut = new FileOutputStream(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+finalname+"/"+fileName));
            }else {
                fileOut = new FileOutputStream(System.getProperty("user.dir")+ Config.getPath(pathName+"/"+name+"/"+fileName));
            }
            // write the new string with the replaced line OVER the same file

            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();
            be.alexandre01.dreamnetwork.client.console.Console.debugPrint("debugPort :" + port);
            return port;

        } catch (Exception e) {
            System.out.println("Problem reading file.");
            e.printStackTrace();
        }
        return null;
    }
    public String getLine(String finalname){
        String name = finalname.split("-")[0];
        File properties = new File(System.getProperty("user.dir")+ Config.getPath("/temp/server/"+name+"/"+finalname+"/logs/latest.log"));
        if(!properties.exists()){
            return null;
        }
        try {

            BufferedReader file = new BufferedReader( new FileReader(System.getProperty("user.dir")+ Config.getPath("/temp/server/"+name+"/"+finalname+"/logs/latest.log")));

            String line = null;

            while ((line = file.readLine()) != null){
                if(line.toLowerCase().startsWith(finalname.toLowerCase())){
                    StringBuilder f = new StringBuilder(line.replace(finalname,""));
                    int c = 0;
                    while (f.charAt(c) == ':' || f.charAt(c) == ' '){
                         f.deleteCharAt(c);
                         c++;
                    }

                    return f.toString();
                }
            }

            file.close();

            // write the new string with the replaced line OVER the same file



        } catch (Exception e) {
            System.out.println("Problem reading file.");
        }
        return null;
    }
    public void addConfigsFiles(){
        if(proxy){
            InputStream is = getClass().getClassLoader().getResourceAsStream("files/bungeecord/config.yml");
            try {
                assert is != null;
                Config.write(is,new File(Config.getPath(System.getProperty("user.dir")+"/template/"+pathName+"/"+name+"/config.yml")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        InputStream ies = getClass().getClassLoader().getResourceAsStream("files/spigot/eula.txt");
        InputStream iss = getClass().getClassLoader().getResourceAsStream("files/spigot/server.properties");
        try {
            assert ies != null;
            Config.write(ies,new File(Config.getPath(System.getProperty("user.dir")+"/template/"+pathName+"/"+name+"/eula.txt")));
            assert iss != null;
            Config.write(iss,new File(Config.getPath(System.getProperty("user.dir")+"/template/"+pathName+"/"+name+"/server.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void updateConfigFile(String pathName, String finalName, JVMExecutor.Mods type, String Xms, String Xmx, int port, boolean proxy,String exec, String startup){
        Console.print("PN>"+pathName, Level.FINE);
        Console.print("FN>"+finalName,Level.FINE);
        Console.print("MODS>"+type.name(),Level.FINE);
        Console.print("XMS>"+Xms,Level.FINE);
        Console.print("XMX>"+Xmx,Level.FINE);
        Console.print("PORT>"+port,Level.FINE);
        Console.print("PROXY>"+proxy,Level.FINE);
        Console.print("STARTUP>"+startup,Level.FINE);
        // Client.getLogger().
        Config.createFile((System.getProperty("user.dir")+"/template/"+pathName+"/"+finalName+"/network.yml"));
        String cTypeName = getLine("type");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(System.getProperty("user.dir")+ Config.getPath("/template/"+pathName+"/"+finalName+"/network.yml"),"utf-8");

            //  System.out.println(type.name());
            writer.println("# "+finalName+"'s configuration of the startup -|- DreamNetwork™ "+ (new Date().getYear()+1900));
            if(type != null){
                writer.println("type: "+type.name());
            }
            if(Xms != null){
                writer.println("xms: "+Xms);
            }
            if(Xmx != null){
                writer.println("xmx: "+Xmx);
            }
            if(port != 0){
                writer.println("port: "+port);
            }
            if(exec != null){
                writer.println("executable: "+exec);
            }
            if(startup != null){
                writer.println("startup: "+startup);
            }
            if(exec != null){
                writer.println("executable: "+exec);
            }
            writer.println("proxy: "+proxy);
            writer.close();

            confSize = getConfigSize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public long getConfigSize(){
        return  Config.createFile((System.getProperty("user.dir")+"/template/"+pathName+"/"+name+"/network.yml")).length();
    }
    public boolean hasExecutable(){
        if(Config.contains(System.getProperty("user.dir")+Config.getPath("/template/"+pathName+"/"+name+"/"+exec))){
            return true;
        }
        return false;
    }
}
