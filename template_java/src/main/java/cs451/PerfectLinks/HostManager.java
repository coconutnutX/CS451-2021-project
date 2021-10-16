package main.java.cs451.PerfectLinks;

import java.util.HashMap;
import java.util.List;

import cs451.Host;

/**
 * Manages all host info
 * initiated when process starts
 * Singleton
 */
public class HostManager {

    private static HostManager instance = new HostManager();
    private HostManager(){}

    private HashMap<Integer, Host> hostMap;

    public static HostManager getInstance(){
        return instance;
    }

    public void init(List<Host> hosts){
        hostMap = new HashMap<Integer, Host>();

        // store host info in HashMap
        for (Host host: hosts) {
            hostMap.put(host.getId(), host);
        }
    }

    public Host getHostById(int id){
        return hostMap.get(id);
    }

}
