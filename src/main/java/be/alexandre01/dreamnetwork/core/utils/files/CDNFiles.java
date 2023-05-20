package be.alexandre01.dreamnetwork.core.utils.files;

import be.alexandre01.dreamnetwork.core.addons.AddonDowloaderObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class CDNFiles {
    public static HashMap<String, AddonDowloaderObject> getAddons(){
        String addonsJSONString = read("addon/officials.json");
        if(addonsJSONString.equals("INVALID FILE")){return new HashMap<>();}
        JsonObject element = new JsonParser().parse(addonsJSONString).getAsJsonObject();
        HashMap<String, AddonDowloaderObject> addons = new HashMap<>();
        for(String name : element.keySet()){
            JsonObject addonInfo = element.getAsJsonObject(name);
            String author = addonInfo.get("author").getAsString();
            String desc = addonInfo.get("description").getAsString();
            System.out.println(addonInfo.get("description"));
            String version = addonInfo.get("version").getAsString();
            String github = addonInfo.get("github").getAsString();
            String downloadLink = addonInfo.get("download").getAsString();
            addons.put(name, new AddonDowloaderObject(name, author, desc, version, github, downloadLink));
        }
        return addons;
    }

    public static void getVersions(){
        String versionsJSONString = read("versions/versions.json");
        if(versionsJSONString.equals("INVALID FILE")){

        }
        JsonObject element = new JsonParser().parse(versionsJSONString).getAsJsonObject();

    }

    private static String read(String file){
        try {
            URL url = new URL("https://cdn.dreamnetwork.cloud/hypervisor/" + file);
            Scanner sc = new Scanner(url.openStream());
            StringBuilder sb = new StringBuilder();
            while (sc.hasNextLine()) {sb.append(sc.nextLine());}
            return sb.toString();
        } catch (IOException e) {
            return "INVALID FILE";
        }
    }
}
