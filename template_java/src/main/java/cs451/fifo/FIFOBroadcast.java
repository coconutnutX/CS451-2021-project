package main.java.cs451.fifo;

import cs451.Constants;
import main.java.cs451.tool.HostManager;
import main.java.cs451.tool.OutputManager;
import main.java.cs451.urb.UniformReliableBroadcast;
import cs451.Host;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Module:
 * Name: FIFOReliableBroadcast, instance frb.
 *
 * Events:
 * Request: ⟨ frb, Broadcast | m ⟩: Broadcasts a message m to all processes.
 * Indication: ⟨ frb, Deliver | p, m ⟩: Delivers a message m broadcast by process p.
 *
 * Properties:
 * FRB1–FRB4: Same as properties RB1–RB4 in (regular) reliable broadcast (Mod- ule 3.2).
 * FRB5: FIFO delivery: If some process broadcasts message m1 before it broadcasts message m2,
 * then no correct process delivers m2 unless it has already delivered m1.
 */
public class FIFOBroadcast {

    private static FIFOBroadcast instance = new FIFOBroadcast();
    private FIFOBroadcast(){};

    private int myId;                     // id of current process
    private Host myHost;                  // host of current process
    private int currentFIFOSEQ;           // keep track of FIFOSEQ of this process

    private Map<Integer, Set<Integer>> pending;         // <CreaterId, <SEQ>>
    private AtomicInteger[] next;                       // next SEQ from each process

    private UniformReliableBroadcast uniformReliableBroadcast;      // base on uniform reliable broadcast

    private AtomicInteger pendingNum;

    public static FIFOBroadcast getInstance() {
        return instance;
    }

    public void init(int myId, Host myHost){
        this.myId = myId;
        this.myHost = myHost;
        this.currentFIFOSEQ = 1;
        this.pending = new ConcurrentHashMap<>();
        this.pendingNum = new AtomicInteger(0);

        // init next, start with 1
        int size = HostManager.getInstance().getTotalHostNumber() + 1; // host id from 1
        next = new AtomicInteger[size];
        for(int i =0; i<size; i++){
            next[i] = new AtomicInteger(1);
        }

        // init UniformReliableBroadcast (Singleton)
        uniformReliableBroadcast = UniformReliableBroadcast.getInstance();
        uniformReliableBroadcast.init(myId, myHost);

        // init pending map
        for(Host host: HostManager.getInstance().getAllHosts()){
            pending.put(host.getId(), new ConcurrentHashMap<>().newKeySet());
        }
    }

    /**
     * Request: ⟨ frb, Broadcast | m ⟩: Broadcasts a message m to all processes.
     */
    public void request(int createrId, int SEQ){
        String logStr = "b " + SEQ + "\n";
        // log broadcast
        if(cs451.Constants.DEBUG_OUTPUT_FIFO){
            System.out.print("[fifo]  "+logStr);
        }
        if(cs451.Constants.WRITE_LOG_FIFO){
            OutputManager.getInstance().addLogBuffer(logStr);
        }

        // call urb request
//        if(Constants.ACTIVATE_URB_BUFFER){
//            uniformReliableBroadcast.bufferedRequest(createrId, SEQ);
//        }else{
//            uniformReliableBroadcast.request(createrId, SEQ);
//        }

        // add pending count
        pendingNum.incrementAndGet();
    }

    /**
     * Indication: ⟨ frb, Deliver | p, m ⟩: Delivers a message m broadcast by process p.
     */
    public void indication(int createrId, int SEQ){
        // add to pending
        pending.get(createrId).add(SEQ);

        // check if can deliver message from this creater
        checkDeliver(createrId);
    }

    public int getAndIncreaseFIFOSEQ(){
        return currentFIFOSEQ++;
    }

    public void checkDeliver(int createrId){
        Set<Integer> currentPending = pending.get(createrId);
        AtomicInteger currentNext = next[createrId];

        // has key and removed, return true
        while(currentPending.remove(currentNext.get())){
            // now currentNext still has old value, no other process can remove
            String logStr = "d " + createrId + " " + currentNext.get() +"\n";

            // log deliver
            if(cs451.Constants.DEBUG_OUTPUT_FIFO){
                System.out.print("[fifo]  "+logStr);
            }
            if(cs451.Constants.WRITE_LOG_FIFO){
                OutputManager.getInstance().addLogBuffer(logStr);
            }

            // decrease pending number
            if(createrId == myId){
                pendingNum.decrementAndGet();
            }

            // increment concurrentNext, could compete to enter
            currentNext.getAndIncrement();
        }
    }

    public int getPendingNum(){
        return pendingNum.get();
    }

    public int getSelfDeliveredNum(){
        return next[myId].get();
    }

}
