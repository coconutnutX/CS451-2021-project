package main.java.cs451.lcb;

import main.java.cs451.tool.HostManager;
import main.java.cs451.tool.OutputManager;
import main.java.cs451.urb.URBMessage;
import main.java.cs451.urb.UniformReliableBroadcast;
import cs451.Host;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    private boolean[] depend;             // depend[i] == true -> current process depend on i

    private AtomicInteger[] vectorClock;            // delivered message vector clock
    private AtomicInteger[] dependVectorClock;      // dependency associated with broadcast message
    private String vectorClockStr;        // concat with '-'
    private String dependVectorClockStr;  // concat with '-'
    private Map<Integer, ConcurrentHashMap<Integer, URBMessage>> pending;         // <CreaterId, <SEQ, message>>

    private UniformReliableBroadcast uniformReliableBroadcast;      // base on uniform reliable broadcast

    private AtomicInteger pendingNum;

    private Thread checkDeliverThread;    // thread to listen to sockets

    public void init(int myId, Host myHost, boolean[] depend){
        this.myId = myId;
        this.myHost = myHost;
        this.depend = depend;
        this.currentSEQ = 1;
        this.totalHost = HostManager.getInstance().getTotalHostNumber();
        this.pending = new ConcurrentHashMap<>();
        this.pendingNum = new AtomicInteger(0);
        this.vectorClock = new AtomicInteger[totalHost];
        this.dependVectorClock = new AtomicInteger[totalHost];
        for(int i=0;i<totalHost;i++){
            vectorClock[i] = new AtomicInteger(0);
            dependVectorClock[i] = new AtomicInteger(0);
        }
        buildVectorClockStr();
        buildDependVectorClockStr();

        // init UniformReliableBroadcast (Singleton)
        uniformReliableBroadcast = UniformReliableBroadcast.getInstance();
        uniformReliableBroadcast.init(myId, myHost);

        // init check deliver thread
//        this.checkDeliverThread = new CheckLegacyDeliverThread();
//        checkDeliverThread.start();

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
            System.out.print("[lcb]["+vectorClockStr+"]   b " + SEQ + " "+dependVectorClockStr+"\n");
        }
        if(cs451.Constants.WRITE_LOG_LCB){
            OutputManager.getInstance().addLogBuffer(logStr);
        }

        uniformReliableBroadcast.request(new URBMessage(createrId, SEQ, dependVectorClockStr));

        // add self vector clock
        updateDependVectorClock(myId);

        // add pending count
        pendingNum.incrementAndGet();
    }

    /**
     * Indication: ⟨ rcb, Deliver | p, m ⟩: Delivers a message m broadcast by process p.
     */
    public void indication(URBMessage urbMessage){
        // add to pending
        pending.get(urbMessage.createrId).put(urbMessage.SEQ, urbMessage);

        // check if can deliver message from this creater
        checkDeliver(urbMessage.createrId);
    }

    public boolean checkDeliver(int createrId){
        ConcurrentHashMap<Integer, URBMessage> currentPending = pending.get(createrId);
        boolean hasDeilivered = false;

        // traverse according to SEQ of this creator
        while(currentPending.containsKey(vectorClock[createrId-1].get()+1)){
            URBMessage urbMessage = currentPending.get(vectorClock[createrId-1].get()+1);
            // check dependency
            int i;
            boolean flag = true;
            for(i=1; i<=totalHost; i++){
                if(vectorClock[i-1].get() < urbMessage.vectorClock[i-1]){
                    flag = false;
                    break;
                }
            }

            if(cs451.Constants.DEBUG_OUTPUT_LCB_CHECK){
                System.out.print("[lcb]["+vectorClockStr+"]   c " + urbMessage.createrId + " " + urbMessage.SEQ + " "+urbMessage.vectorClockStr+" "+flag+"\n");
            }

            if(flag==false){
                // has dependency
                System.out.println("depend on "+i);
                // check if dependency can be solved
                if(createrId==myId || checkDeliver(i)==false){
                    // nothing delivered
                    break;
                }
            }else{
                deliver(urbMessage);
                hasDeilivered = true;
            }
        }

        return hasDeilivered;
    }

    public void deliver(URBMessage urbMessage){
        // only deliver when remove from pending succeed, prevent concurrent access
        if(pending.get(urbMessage.createrId).remove(urbMessage.SEQ) != null){
            String logStr = "d " + urbMessage.createrId + " " + urbMessage.SEQ + "\n";

            // if has dependency, update dependency vector clock
            if(depend[urbMessage.createrId]){
                updateDependVectorClock(urbMessage.createrId);
            }

            // log broadcast
            if(cs451.Constants.DEBUG_OUTPUT_LCB){
                System.out.print("[lcb]["+vectorClockStr+"]   d " + urbMessage.createrId + " " + urbMessage.SEQ + " "+urbMessage.vectorClockStr+"\n");
            }
            if(cs451.Constants.WRITE_LOG_LCB){
                OutputManager.getInstance().addLogBuffer(logStr);
            }

            // update vector clock
            updateVectorClock(urbMessage.createrId);

            // if is message from current process, decrease pendingNum
            if(urbMessage.createrId == myId){
                pendingNum.decrementAndGet();
            }
        }
    }

    private void updateVectorClock(int createrId) {
        vectorClock[createrId-1].getAndIncrement();    // id start from 1
        buildVectorClockStr();
    }

    private void updateDependVectorClock(int createrId) {
        dependVectorClock[createrId-1].getAndIncrement(); // id start from 1
        buildDependVectorClockStr();
    }

    private void buildVectorClockStr(){
        String str = Arrays.toString(vectorClock).replaceAll(",\\s+", "-");
        vectorClockStr = str.substring(1,str.length()-1);
    }

    private void buildDependVectorClockStr(){
        String str = Arrays.toString(dependVectorClock).replaceAll(",\\s+", "-");
        dependVectorClockStr = str.substring(1,str.length()-1);
    }

    public int getAndIncreaseSEQ(){
        return currentSEQ++;
    }

    public int getPendingNum(){
        return pendingNum.get();
    }
}
