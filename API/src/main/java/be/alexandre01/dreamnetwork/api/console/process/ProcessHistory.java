package be.alexandre01.dreamnetwork.api.console.process;

import be.alexandre01.dreamnetwork.api.console.colors.Colors;
import be.alexandre01.dreamnetwork.api.utils.files.json.JSONFileUtils;
import be.alexandre01.dreamnetwork.api.utils.process.ProcessUtils;
import com.google.gson.Gson;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ProcessHistory {
    @Getter protected static List<Long> dnProcess = new ArrayList<>();
    @Getter protected static List<Long> servicesProcess = new ArrayList<>();
    private final File tokenFile = new File(System.getProperty("user.dir")+"/data/ProcessHistory.json");
   @Getter
   private ProcessHistoryIndex processHistoryIndex;

    public static String convert(List<Long> s){
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.size(); i++) {
            long w = s.get(i);
            sb.append(w);
            if(i != s.size()-1){
                sb.append(";");
            }
        }
        return sb.toString();
    }

    public void init(){
        if(!tokenFile.exists()){
            try {
                tokenFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Gson gson = new Gson();
        try {
            Reader reader = Files.newBufferedReader(Paths.get(tokenFile.getAbsolutePath()));
            processHistoryIndex = gson.fromJson(reader, ProcessHistoryIndex.class);
            if(processHistoryIndex == null)
                processHistoryIndex = new ProcessHistoryIndex();
            processHistoryIndex.setIndexFile(tokenFile);

            if(!processHistoryIndex.isEmpty()){
                for (long i : ProcessHistory.getDnProcess()) {
                    if(ProcessUtils.isStillAllive(i)){
                        ProcessUtils.killProcess(i);
                        System.out.println(Colors.RED+"Killed Another DreamNetwork process on the machine - PID :"+i);
                        try {
                            System.out.println("Waiting to kill the process...");
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
                boolean b = false;
                for (long i : ProcessHistory.getServicesProcess()) {
                    if(ProcessUtils.isStillAllive(i)){
                        b = true;
                        System.out.println(Colors.RED+"Killed old services process on the machine - PID :"+i);
                        ProcessUtils.killProcess(i);
                    }
                }
                if(b){
                    try {
                        System.out.println("Waiting to kill the process...");
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            ArrayList<Long> a = new ArrayList<>();
            try {
                a.add(ProcessUtils.getCurrentID());
            } catch (Exception ignored) {
                // ignore
            }

            processHistoryIndex.put("DNProcess", Base64.getEncoder().encodeToString(convert(a).getBytes(StandardCharsets.UTF_8)));
            processHistoryIndex.refreshFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static class ProcessHistoryIndex extends JSONFileUtils {
        @Override
        public Object put(String key, Object value) {
            Object k = super.put(key, value);
            if(key.equalsIgnoreCase("DNProcess")){
                if(value instanceof String){
                    String s = (String) value;

                    s = new String(Base64.getDecoder().decode(s));
                       for (long i : getFrom(s)) {
                            ProcessHistory.getDnProcess().add(i);
                        }
                }
            }
            if(key.equalsIgnoreCase("TMPProcess")){
                if(value instanceof String){
                    String s = (String) value;
                    s = new String(Base64.getDecoder().decode(s));
                    for (long i : getFrom(s)) {
                        ProcessHistory.getServicesProcess().add(i);
                    }
                }
            }
            return k;
        }

        private List<Long> getFrom(String s){
            List<Long> list = new ArrayList<>();
            String[] split = s.split(";");
            for(String h : split){
                try {
                    Long.parseLong(h);
                } catch (NumberFormatException e) {
                    continue;
                }
                list.add(Long.parseLong(h));
            }
            return list;
        }
    }
}
