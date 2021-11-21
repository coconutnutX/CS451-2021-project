package main.java.cs451.urb;

import main.java.cs451.fifo.FIFOBroadcast;
import main.java.cs451.tool.HostManager;
import main.java.cs451.pl.PerfectLink;
import main.java.cs451.pl.PerfectLinkMessage;

import cs451.Host;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Module:
 * Name: UniformReliableBroadcast, instance urb.
 *
 * Events:
 * Request: < urb, Broadcast | m >: Broadcasts a message m to all processes.
 * Indication: < urb, Deliver | p, m >: Delivers a message m broadcast by process p.
 *
 * Properties:
 * URB1: Validity: If a correct process p broadcasts a message m, then p eventually delivers m.
 * URB2: No duplication: No message is delivered more than once.
 * URB3: No creation: If a process delivers a message m with sender s, then m was previously broadcast by process s.
 * URB4: Uniform agreement: If a message m is delivered by some process (whether correct or faulty), then m is eventually delivered by every correct process.
 *
 * Singleton
 */
public class UniformReliableBroadcast {

    private static UniformReliableBroadcast instance = new UniformReliableBroadcast();
    private UniformReliableBroadcast(){};

    private int myId;                     // id of current process
    private cs451.Host myHost;            // host of current process

    private PerfectLink perfectLink;      // base on perfect link

    private Map<Integer, Map<Integer, BitSet>> pending;   // <CreaterId, <SEQ, URBMessage>>
    private Map<Integer, Set<Integer>> delivered;         // <CreaterId, <SEQ>>
    private int bitSetSize;
    private int majorityNum;

    private ConcurrentLinkedQueue<URBMessage> buffer;         // messages to send (control speed of sending message generated from current process)
    private Thread urbMessageBufferSender;                    // thread to send URB messages

    public static UniformReliableBroadcast getInstance(){
        return instance;
    }

    public void init(int myId, cs451.Host myHost){
        this.myId = myId;
        this.myHost = myHost;
        this.pending = new HashMap<>();
        this.delivered = new HashMap<>();
        this.buffer = new ConcurrentLinkedQueue<>();
        this.bitSetSize = HostManager.getInstance().getTotalHostNumber();
        this.majorityNum = bitSetSize / 2;

        // init PerfectLinks (Singleton)
        perfectLink = PerfectLink.getInstance();
        perfectLink.init(myId, myHost);

        // init pending and delivered map
        for(Host host: HostManager.getInstance().getAllHosts()){
            pending.put(host.getId(), new ConcurrentHashMap<>());
            delivered.put(host.getId(), new ConcurrentHashMap<>().newKeySet());
        }

        if(cs451.Constants.ACTIVATE_URB_BUFFER){
            // init URB message buffer sender
            urbMessageBufferSender = new URBMessageBufferSender(buffer);
            urbMessageBufferSender.start();
        }
    }

    public void bufferedRequest(int createrId, int SEQ){
        URBMessage urbMessage = new URBMessage(createrId, SEQ);
        // use sliding window to reduce message flow
        buffer.add(urbMessage);
    }

    /**
     * Request: < urb, Broadcast | m >: Broadcasts a message m to all processes.
     */
    public void request(int createrId, int SEQ){
        // add message to pending, and add ack count
        Map<Integer, BitSet> currentPending = pending.get(myId);
        currentPending.putIfAbsent(SEQ, new BitSet());
        currentPending.get(SEQ).set(myId - 1); // set id-1 bit to 1

        // log broadcast
        if(cs451.Constants.DEBUG_OUTPUT_URB){
            String logStr = "b " + SEQ + "\n";
            System.out.print("[urb]  "+logStr);
        }

        // send message to all other hosts
        for(Host desHost: HostManager.getInstance().getAllHosts()){
            // skip itself
            if(desHost.getId() == myId){
                continue;
            }

            // increase PSEQ each time, ensure unique PSEQ
            PerfectLinkMessage perfectLinkMessage = new PerfectLinkMessage(desHost, myHost, myHost, SEQ, perfectLink.getAndIncreasePSEQ());
            perfectLink.request(perfectLinkMessage);
        }
    }

    /**
     * Indication: < urb, Deliver | p, m >: Delivers a message m broadcast by process p.
     */
    public void indication(PerfectLinkMessage perfectLinkMessage){
        Host creater = perfectLinkMessage.getCreater();
        int createrId = creater.getId();
        int SEQ = perfectLinkMessage.getSEQ();

        // already delivered, return
        if(delivered.get(createrId).contains(SEQ)){
            return;
        }

        Map<Integer, BitSet> currentPending = pending.get(createrId);
        // not in pending, relay
        if(!currentPending.containsKey(SEQ)){
            // new ack count
            currentPending.putIfAbsent(SEQ, new BitSet());

            // log broadcast
            if(cs451.Constants.DEBUG_OUTPUT_URB_RELAY){
                String logStr = "b " + SEQ + "\n";
                System.out.print("[urbr] "+logStr);
            }

            // relay message to all other hosts
            for(Host desHost: HostManager.getInstance().getAllHosts()){
                // skip itself
                if(desHost.getId() == myId){
                    continue;
                }

                // increase PSEQ each time, ensure unique PSEQ
                PerfectLinkMessage relayPerfectLinkMessage = new PerfectLinkMessage(desHost, creater, myHost, SEQ, perfectLink.getAndIncreasePSEQ());
                perfectLink.request(relayPerfectLinkMessage);
            }
        }

        // add ack count
        currentPending.get(SEQ).set(perfectLinkMessage.getSender().getId() - 1); // set id-1 bit to 1

        // check if can deliver
        if(currentPending.get(SEQ).cardinality() > majorityNum){
            deliver(createrId, SEQ);
        }
    }

    public void deliver(int createrId, int SEQ){
        // log deliver
        if(cs451.Constants.DEBUG_OUTPUT_URB){
            String logStr = "d " + createrId + " " + SEQ + "\n";
            System.out.print("[urb]  "+logStr);
        }

        // add to delivered
        delivered.get(createrId).add(SEQ);

        // remove from pending
        pending.get(createrId).remove(SEQ);

        // call FIFO indication
        FIFOBroadcast.getInstance().indication(createrId, SEQ);
    }

    public int getSelfDeliveredNum(){
        return delivered.get(myId).size();
    }

    public int getSelfPendingNum(){
        return pending.get(myId).size();
    }

}
