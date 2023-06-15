package be.alexandre01.dreamnetwork.core.service.deployment;

import be.alexandre01.dreamnetwork.core.Main;
import be.alexandre01.dreamnetwork.core.utils.files.yaml.Ignore;
import be.alexandre01.dreamnetwork.core.utils.files.yaml.YamlFileUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter @Setter
public class DeployData extends YamlFileUtils<DeployData> implements Deploy{

    @Ignore private String name;
    @Ignore private File directory;

    private String author = Main.getGlobalSettings().getUsername();
    private String[] types =  new String[]{"CONFIGURATION"};
    private String compatibleVersions = "UNKNOWN";
    private String version = "1.0";



    public DeployData(){
        // Init

    }
    public boolean loading(File file){
        addAnnotation("Deployment folder for Services");

        directory = file.getParentFile();
        name = directory.getName();
        if(!super.config(file, DeployData.class,true)){
            super.saveFile(DeployData.class.cast(this));
        }else {
            super.readAndReplace(this);
            save();
        }
        return true;
    }

    public void save(){
        super.saveFile(DeployData.class.cast(this));
    }

    public DeployType[] getDeployTypes(){
        return Arrays.stream(types).map(DeployType::valueOf).toArray(DeployType[]::new);
    }



    public enum DeployType{
        CONFIGURATIONS,
        ONLY_MAP,
        CUSTOM,
        EXEC_JAR,
        ALL_IN_ONE
    }
}
