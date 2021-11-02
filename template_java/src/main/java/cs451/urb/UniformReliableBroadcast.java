package main.java.cs451.urb;

import main.java.cs451.pl.HostManager;
import main.java.cs451.pl.PerfectLink;
import main.java.cs451.pl.PerfectLinkMessage;

import cs451.Host;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
    private UniformReliableBroadcast(){}

    private int myId;                     // id of current process
    private cs451.Host myHost;            // host of current process
    private int currentURBSEQ;            // keep track of URBSEQ of this process, should be unique in all processes

    private StringBuffer logBuffer;       // store log, write to file when terminate
    private String outputPath;            // output log file path

    private PerfectLink perfectLink;      // base perfect link

    private Map<Integer, Map<Integer, URBMessage>> pending; // <CreaterId, <SEQ, URBMessage>>

    // delivered


    public static UniformReliableBroadcast getInstance(){
        return instance;
    }

    public void init(int myId, cs451.Host myHost, String outputPath){
        this.myId = myId;
        this.myHost = myHost;
        this.currentURBSEQ = 0;
        this.logBuffer = new StringBuffer();
        this.outputPath = outputPath;
        this.pending = new HashMap<>();

        // init PerfectLinks (Singleton)
        perfectLink = PerfectLink.getInstance();
        perfectLink.init(myId, myHost, outputPath);
    }

    public void addToPending(int createrId, URBMessage urbMessage){
        if(!pending.containsKey(createrId)){
            pending.put(createrId, new HashMap<>());
        }

        pending.get(createrId).put(urbMessage.getSEQ(), urbMessage);
    }

    public boolean isInPending(int createrId, int SEQ){
        if(!pending.containsKey(createrId)){
            pending.put(createrId, new HashMap<>());
        }

        return pending.get(createrId).containsKey(SEQ);
    }

    public URBMessage getOrCreateURBMessageInPending(int createrId, int SEQ){
        if(!pending.containsKey(createrId)){
            pending.put(createrId, new HashMap<>());
        }

        Map<Integer, URBMessage> createrPending = pending.get(createrId);
        if(!createrPending.containsKey(SEQ)){
            // construct URB msg
            URBMessage urbMessage = new URBMessage(createrId, SEQ);

            // add to pending
            createrPending.put(SEQ, urbMessage);
        }

        return createrPending.get(SEQ);
    }

    /**
     * Request: < urb, Broadcast | m >: Broadcasts a message m to all processes.
     */
    public void request(URBMessage urbMessage){
        // add message to pending
        addToPending(myId, urbMessage);

        // log broadcast
        String logStr = "b " + urbMessage.getSEQ() + "\n";
        if(cs451.Constants.DEBUG_OUTPUT_URB){
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
            String logStr = "b " + SEQ + "\n";
            if(cs451.Constants.DEBUG_OUTPUT_URB){
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
        System.out.println("urb indication, creater="+creater.getId()+" SEQ="+SEQ+" #ack="+urbMessage.ackNumber());
    }

    public int getAndIncreaseURBSEQ(){
        return currentURBSEQ++;
    }

}
