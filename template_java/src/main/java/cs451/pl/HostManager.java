package main.java.cs451.pl;

import java.util.HashMap;
import java.util.List;
import java.util.Collection;

import cs451.Host;

/**
 * Manages all host info
 * Initiated when process starts
 * Singleton
 */
public class HostManager {

    private static HostManager instance = new HostManager();
    private HostManager(){}

    private HashMap<Integer, Host> hostMap;

    public static HostManager getInstance(){
        return instance;
    }

    // store all hosts in map, and return current host
    public Host init(List<Host> hosts, int myId){
        hostMap = new HashMap<Integer, Host>();

        // find host of current process
        Host myHost = null;

        // store host info in HashMap
        for (Host host: hosts) {
            hostMap.put(host.getId(), host);
            if(host.getId() == myId){
                myHost = host;
            }
        }

        System.out.println("HostManager initialized\n");

        return myHost;
    }

    public Host getHostById(int id){
        return hostMap.get(id);
    }

    public Collection<Host> getAllHosts() {
        return hostMap.values();
    }

}
