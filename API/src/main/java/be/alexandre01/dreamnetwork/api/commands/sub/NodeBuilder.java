package be.alexandre01.dreamnetwork.api.commands.sub;

import be.alexandre01.dreamnetwork.api.DNCoreAPI;
import be.alexandre01.dreamnetwork.api.commands.sub.types.CustomType;
import be.alexandre01.dreamnetwork.api.commands.sub.types.NullNode;
import be.alexandre01.dreamnetwork.api.commands.sub.types.TextNode;
import be.alexandre01.dreamnetwork.api.console.Console;
import be.alexandre01.dreamnetwork.api.console.IConsoleReader;
import be.alexandre01.dreamnetwork.api.console.colors.Colors;
import be.alexandre01.dreamnetwork.api.console.jline.completors.CustomTreeCompleter;
import be.alexandre01.dreamnetwork.utils.Tuple;
import lombok.Getter;
import org.jline.reader.Candidate;

import java.util.*;

public class NodeBuilder {
    CustomTreeCompleter.Node node;

    Console console;

    List<Object> globalList;
    int num  =0;
    HashMap<CustomTreeCompleter.Node,Object[]> nodes = new HashMap<>();

    HashMap<String,Object> objects = new HashMap<>();


    public static NullNode EMPTYFIELD = new NullNode();
    @Getter NodeContainer nodeContainer;

    public static NodeContainer create(Object... o){
        return new NodeContainer(o);
    }

    public static NodeContainer create(String... s){
        return new NodeContainer(s);
    }

    public static Candidate of(String value,String display){
        return new Candidate(value,display,null,null,null,null,true);
    }


    public NodeBuilder(NodeContainer nodeContainer, Console console){
        this.console = console;
        this.nodeContainer = nodeContainer;
        node = genNode(nodeContainer,true,false);


        /*for (Map.Entry<CustomTreeCompleter.Node, Object[]> entry : nodes.entrySet()) {
            CustomTreeCompleter.Node key = entry.getKey();
            Object[] value = entry.getValue();
        }*/

        registerTo();
    }
    public NodeBuilder(NodeContainer nodeContainer){
        this(nodeContainer, Console.getConsole("m:default"));
    }


    private void addNode(CustomTreeCompleter.Node node, Object[] objects){
        nodes.put(node,objects);
    }
    public CustomTreeCompleter.Node genNode(NodeContainer nodeContainer,boolean isHead,boolean isReloading){
        final Object objects[] = nodeContainer.getObjects();
        final ArrayList<Object> list = new ArrayList<>();
        nodeContainer.setList(list);
        for (int i = 0; i < objects.length; i++) {
            Object o = objects[i];

            if(o instanceof NodeContainer){
                if(!isReloading)
                    ((NodeContainer) o).getLinksNodeBuilder().add(this);
                CustomTreeCompleter.Node node = genNode((NodeContainer) o,false,isReloading);
                list.add(node);
                continue;
            }

            if(o instanceof CustomType){
                CustomType customType = (CustomType) o;
                customType.setLinkNodeContainers(nodeContainer);
                customType.setGlobalObjects(list);
                int currentSize = list.size();

                Console.getConsoleReader().getDefaultHighlighter();

                list.addAll(Arrays.asList((customType.reload())));
                nodeContainer.getIndex().put(o,new Tuple<>(currentSize,list.size()));

                continue;
            }

            if(o instanceof NullNode){
                list.add(EMPTYFIELD);
                continue;
            }

            if(o instanceof TextNode){
                list.addAll(Arrays.asList(((TextNode) o).getTexts()));
                continue;
            }

            list.add(o);
        }
        //System.out.println("AFTER");
        if(isHead){
            globalList = list;
        }
      //Console.print("Build Suggestion -> " + list);£

       CustomTreeCompleter.Node n = (CustomTreeCompleter.Node) CustomTreeCompleter.node(list.toArray());
        for (int i = 0; i < objects.length; i++) {
            Object o = objects[i];
            if(o instanceof CustomType){
                CustomType customType = (CustomType) o;
                customType.setNodeAbove(n);
            }
        }
        //System.out.println(list);
        nodes.put(n,list.toArray());


        return n;
    }


    public void rebuild(){
        try {
            //Console.fine("rebuild suggestion");

           // log suggestions
           // Console.fine(globalList);
            for (int i = 0; i < globalList.size(); i++) {
                Object o = globalList.get(i);
                if(o instanceof NodeContainer){
                    printContainers((NodeContainer) o);
                }
                //System.out.println(o);
            }

           // System.out.println(num);
            console.completorNodes.set(num, node = genNode(nodeContainer,true,true));


            //Console.getConsoleReader().setNodes(console.completorNodes);
            //ConsoleReader.nodes.set(num, node = genNode(nodeContainer,true));
            //Console.getCurrent().reloadCompletors();
            //ConsoleReader.reloadCompleter();
        }catch (Exception e){
            Console.bug(e);
        }

    }
    private void printContainers(NodeContainer nodeContainer){
        for (int i = 0; i < nodeContainer.getIndex().size(); i++) {
            Object o = nodeContainer.getList().get(i);
            if(o instanceof NodeContainer){
                printContainers((NodeContainer) o);
                continue;
            }
           Console.fine(Colors.RED+o);
        }
    }
    public void registerTo(){
        Console.fine("Register new Suggestion");
        num = console.completorNodes.size();
       // ConsoleReader.nodes.add(node);
        console.completorNodes.add(node);
    }
}
