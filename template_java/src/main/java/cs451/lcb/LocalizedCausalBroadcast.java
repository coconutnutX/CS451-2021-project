package main.java.cs451.lcb;

import main.java.cs451.tool.HostManager;
import main.java.cs451.tool.OutputManager;
import main.java.cs451.urb.URBMessage;
import main.java.cs451.urb.UniformReliableBroadcast;
import cs451.Host;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalizedCausalBroadcast {

    private static LocalizedCausalBroadcast instance = new LocalizedCausalBroadcast();
    private LocalizedCausalBroadcast(){}

    public static LocalizedCausalBroadcast getInstance() {
        return instance;
    };

    private int myId;                     // id of current process
    private Host myHost;                  // host of current process
    private int currentSEQ;               // keep track of SEQ of this process
    private int totalHost;

    private List<Integer> vectorClock;            // vector clock of dependency
    private String vectorClockStr;                // string representation
    private Map<Integer, ConcurrentHashMap<String, Integer>> pending;         // <CreaterId, <vectorClock, SEQ>>

    private UniformReliableBroadcast uniformReliableBroadcast;      // base on uniform reliable broadcast

    private AtomicInteger pendingNum;

    public void init(int myId, Host myHost){
        this.myId = myId;
        this.myHost = myHost;
        this.currentSEQ = 1;
        this.totalHost = HostManager.getInstance().getTotalHostNumber();
        this.pending = new ConcurrentHashMap<>();
        this.pendingNum = new AtomicInteger(0);
        Integer[] initialClock = new Integer[totalHost];
        this.vectorClock = Arrays.asList(initialClock);

        // init UniformReliableBroadcast (Singleton)
        uniformReliableBroadcast = UniformReliableBroadcast.getInstance();
        uniformReliableBroadcast.init(myId, myHost);

        // init pending map
        for(Host host: HostManager.getInstance().getAllHosts()){
            pending.put(host.getId(), new ConcurrentHashMap<>());
        }
    }

    /**
     * Request: ⟨ lcb, Broadcast | m ⟩: Broadcasts a message m to all processes.
     */
    public void request(int createrId, int SEQ){
        String logStr = "b " + SEQ + "\n";
        // log broadcast
        if(cs451.Constants.DEBUG_OUTPUT_LCB){
            System.out.print("[lcb]   "+logStr);
        }
        if(cs451.Constants.WRITE_LOG_LCB){
            OutputManager.getInstance().addLogBuffer(logStr);
        }

        uniformReliableBroadcast.request(new URBMessage(createrId, SEQ, vectorClockStr));

        // add pending count
        pendingNum.incrementAndGet();
    }

    /**
     * Indication: ⟨ rcb, Deliver | p, m ⟩: Delivers a message m broadcast by process p.
     */
    public void indication(URBMessage urbMessage){
        System.out.println("[lcb indi]"+urbMessage.SEQ+" "+urbMessage.vectorClockStr);
        // add to pending
//        pending.get(createrId).add(SEQ);

        // check if can deliver message from this creater
    }

    private void updateVectorClock() {

        // update vector clock string
        vectorClockStr = vectorClock.stream().map(String::valueOf).collect(Collectors.joining("-"));
    }

    public int getAndIncreaseSEQ(){
        return currentSEQ++;
    }

    public int getPendingNum(){
        return pendingNum.get();
    }
}
