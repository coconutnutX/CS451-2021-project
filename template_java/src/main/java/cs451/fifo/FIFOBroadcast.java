package main.java.cs451.fifo;

import main.java.cs451.tool.HostManager;
import main.java.cs451.tool.OutputManager;
import main.java.cs451.urb.URBMessage;
import main.java.cs451.urb.UniformReliableBroadcast;
import cs451.Host;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private cs451.Host myHost;            // host of current process
    private int currentFIFOSEQ;           // keep track of FIFOSEQ of this process

    private Map<Integer, PriorityQueue<FIFOMessage>> pending;     // <CreaterId, <FIFOMessage>>, smallest on top
    private int[] next;                   // next SEQ from each process

    private UniformReliableBroadcast uniformReliableBroadcast;      // base on uniform reliable broadcast

    public static FIFOBroadcast getInstance() {
        return instance;
    }

    public void init(int myId, Host myHost){
        this.myId = myId;
        this.myHost = myHost;
        this.currentFIFOSEQ = 1;
        this.pending = new ConcurrentHashMap<>();

        // init next, fill with 1s
        this.next = new int[HostManager.getInstance().getTotalHostNumber()+1]; // host id from 1
        Arrays.fill(next,1);

        // init UniformReliableBroadcast (Singleton)
        uniformReliableBroadcast = UniformReliableBroadcast.getInstance();
        uniformReliableBroadcast.init(myId, myHost);

        // init pending map
        for(Host host: HostManager.getInstance().getAllHosts()){
            pending.put(host.getId(), new PriorityQueue<>(new Comparator<FIFOMessage>() {
                @Override
                public int compare(FIFOMessage o1, FIFOMessage o2) {
                    return o1.getSEQ() - o2.getSEQ();
                }
            }));
        }
    }

    /**
     * Request: ⟨ frb, Broadcast | m ⟩: Broadcasts a message m to all processes.
     */
    public void request(FIFOMessage fifoMessage){
        String logStr = "b " + fifoMessage.getSEQ() + "\n";
        // log broadcast
        if(cs451.Constants.DEBUG_OUTPUT_FIFO){
            System.out.print("[fifo]  "+logStr);
        }
        if(cs451.Constants.WRITE_LOG_FIFO){
            OutputManager.getInstance().addLogBuffer(logStr);
        }

        // call urb request
        URBMessage urbMessage = new URBMessage(fifoMessage.getCreaterId(), fifoMessage.getSEQ());
        uniformReliableBroadcast.request(urbMessage);
    }

    /**
     * Indication: ⟨ frb, Deliver | p, m ⟩: Delivers a message m broadcast by process p.
     */
    public synchronized void indication(URBMessage urbMessage){
        // add to pending
        PriorityQueue<FIFOMessage> queue = pending.get(urbMessage.getCreaterId());
        FIFOMessage fifoMessage = new FIFOMessage(urbMessage.getCreaterId(), urbMessage.getSEQ());
        queue.add(fifoMessage);

        // check if can deliver message from this creater
        checkDeliver(urbMessage.getCreaterId());
    }

    public int getAndIncreaseFIFOSEQ(){
        return currentFIFOSEQ++;
    }

    public synchronized void checkDeliver(int createrId){
        PriorityQueue<FIFOMessage> queue = pending.get(createrId);
        while(!queue.isEmpty()){
            // check if is next message
            if(queue.peek().getSEQ() == next[createrId]){
                deliver(queue.peek());
                // update next
                next[createrId]++;
                // remove from queue
                queue.remove();
            }else{
                break;
            }
        }
    }

    public void deliver(FIFOMessage fifoMessage){
        String logStr = "d " + fifoMessage.getCreaterId() + " " + fifoMessage.getSEQ() +"\n";
        // log deliver
        if(cs451.Constants.DEBUG_OUTPUT_FIFO){
            System.out.print("[fifo]  "+logStr);
        }
        if(cs451.Constants.WRITE_LOG_FIFO){
            OutputManager.getInstance().addLogBuffer(logStr);
        }
    }

}
