package main.java.cs451.urb;

import main.java.cs451.fifo.FIFOBroadcast;
import main.java.cs451.tool.HostManager;
import main.java.cs451.pl.PerfectLink;
import main.java.cs451.pl.PerfectLinkMessage;

import cs451.Host;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private int currentURBSEQ;            // keep track of URBSEQ of this process, should be unique in all processes

    private PerfectLink perfectLink;      // base on perfect link

    private Map<Integer, Map<Integer, URBMessage>> pending;   // <CreaterId, <SEQ, URBMessage>>
    private Map<Integer, Set<Integer>> delivered;             // <CreaterId, <SEQ>>


    public static UniformReliableBroadcast getInstance(){
        return instance;
    }

    public void init(int myId, cs451.Host myHost){
        this.myId = myId;
        this.myHost = myHost;
        this.currentURBSEQ = 1;
        this.pending = new ConcurrentHashMap<>();
        this.delivered = new ConcurrentHashMap<>();

        // init PerfectLinks (Singleton)
        perfectLink = PerfectLink.getInstance();
        perfectLink.init(myId, myHost);

        // init pending and delivered map
        for(Host host: HostManager.getInstance().getAllHosts()){
            pending.put(host.getId(), new ConcurrentHashMap<>());
            delivered.put(host.getId(), new HashSet<>());
        }
    }

    /**
     * Request: < urb, Broadcast | m >: Broadcasts a message m to all processes.
     */
    public void request(URBMessage urbMessage){
        // add message to pending
        addToPending(myId, urbMessage);

        // log broadcast
        if(cs451.Constants.DEBUG_OUTPUT_URB){
            String logStr = "b " + urbMessage.getSEQ() + "\n";
            System.out.print("[urb]  "+logStr);
        }

        // send message to all other hosts
        for(Host desHost: HostManager.getInstance().getAllHosts()){
            // skip itself
            if(desHost.getId() == myId){
                continue;
            }

            // increase PSEQ each time, ensure unique PSEQ
            PerfectLinkMessage perfectLinkMessage = new PerfectLinkMessage(desHost, myHost, myHost, urbMessage.getSEQ(), perfectLink.getAndIncreasePSEQ());
            perfectLink.request(perfectLinkMessage);
        }
    }

    /**
     * Indication: < urb, Deliver | p, m >: Delivers a message m broadcast by process p.
     */
    public void indication(PerfectLinkMessage perfectLinkMessage){
        Host creater = perfectLinkMessage.getCreater();
        int SEQ = perfectLinkMessage.getSEQ();

        // not in pending, relay
        if(!isInPending(creater.getId(), SEQ)){
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

        // get or add to pending
        URBMessage urbMessage = getOrCreateURBMessageInPending(creater.getId(), SEQ);
        // add ack
        urbMessage.addAck(perfectLinkMessage.getSender().getId());

        // check if can deliver
        if(canDeliver(urbMessage)){
            deliver(urbMessage);
        }
    }

    public void deliver(URBMessage urbMessage){
        // log deliver
        if(cs451.Constants.DEBUG_OUTPUT_URB){
            String logStr = "d " + urbMessage.getCreaterId() + " " + urbMessage.getSEQ() + "\n";
            System.out.print("[urb]  "+logStr);
        }

        // add to delivered
        delivered.get(urbMessage.getCreaterId()).add(urbMessage.getSEQ());

        // call FIFO indication
        FIFOBroadcast.getInstance().indication(urbMessage);
    }

    public int getAndIncreaseURBSEQ(){
        return currentURBSEQ++;
    }

    public boolean canDeliver(URBMessage urbMessage){
        // 1.message is in pending

        // 2.ack > N/2
        int ackNum = urbMessage.ackNumber();
        int majorityNum = HostManager.getInstance().getMajorityNumber();
        boolean cond1 =  ackNum > majorityNum;

        if(cond1 == false){
            return false;
        }

        // 3.not delivered already
        boolean cond2 = delivered.get(urbMessage.getCreaterId()).contains(urbMessage.getSEQ());

        if(cond2 == true){
            return false;
        }

        return true;
    }

    public void addToPending(int createrId, URBMessage urbMessage){
        pending.get(createrId).put(urbMessage.getSEQ(), urbMessage);
    }

    public boolean isInPending(int createrId, int SEQ){
        return pending.get(createrId).containsKey(SEQ);
    }

    public URBMessage getOrCreateURBMessageInPending(int createrId, int SEQ){
        Map<Integer, URBMessage> createrPending = pending.get(createrId);
        if(!createrPending.containsKey(SEQ)){
            // construct URB msg
            URBMessage urbMessage = new URBMessage(createrId, SEQ);

            // add to pending
            createrPending.put(SEQ, urbMessage);
        }

        return createrPending.get(SEQ);
    }

}
