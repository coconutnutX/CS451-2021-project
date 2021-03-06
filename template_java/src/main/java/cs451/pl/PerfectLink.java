package main.java.cs451.pl;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cs451.Host;
import main.java.cs451.tool.HostManager;
import main.java.cs451.urb.UniformReliableBroadcast;

/***
 * Module:
 * Name: PerfectPointToPointLinks, instance pl.
 *
 * Events:
 * Request: ⟨ pl, Send | q, m ⟩: Requests to send message m to process q.
 * Indication: ⟨ pl, Deliver | p, m ⟩: Delivers message m sent by process p.
 *
 * Properties:
 * PL1: Reliable delivery: If a correct process p sends a message m to a correct process q, then q eventually delivers m.
 * PL2: No duplication: No message is delivered by a process more than once.
 * PL3: No creation: If some process q delivers a message m with sender p, then m
 * was previously sent to q by process p.
 *
 * Singleton
 */
public class PerfectLink {

    private static PerfectLink instance = new PerfectLink();
    private PerfectLink(){}

    private int myId;               // id of current process
    private Host myHost;            // host of current process
    private AtomicInteger currentPSEQ;        // keep track of PSEQ of this process, should be unique in all processes

    private Thread socketServer;    // thread to listen to sockets
    private Thread messageResender; // thread to resend sockets periodically

    private HashMap<Integer, ConcurrentHashMap<Integer, Integer>> delivered; // messages delivered, <sender ID, <PSEQ, 0>>

    public static PerfectLink getInstance(){
        return instance;
    }

    public void init(int myId, Host myHost) {
        this.myId = myId;
        this.myHost = myHost;
        this.currentPSEQ = new AtomicInteger(1);
        this.socketServer = new SocketServer(myId, myHost);
        this.messageResender = new MessageResender();
        this.delivered = new HashMap<>();

        // init delivered
        for(Host host: HostManager.getInstance().getAllHosts()){
            delivered.put(host.getId(), new ConcurrentHashMap<>());
        }

        // start listen to port
        socketServer.start();

        // check periodically to resend messages
        messageResender.start();

        System.out.println("PerfectLink initialized\n");
    }

    // request to send message perfectLinkMessage
    public void request(PerfectLinkMessage perfectLinkMessage){
        // log info
        if(!perfectLinkMessage.isResend){
            String logStr = "b " + perfectLinkMessage.SEQ + "\n";
            // PerfectLink.getInstance().addLogBuffser(logStr);
            if(cs451.Constants.DEBUG_OUTPUT_PL){
                System.out.print("[pl]   "+logStr);
            }
        }

        // send message
        SocketClient socketClient = new SocketClient();
        socketClient.sendMessage(perfectLinkMessage);

        // mark resend, only change once when first send
        if(!perfectLinkMessage.isResend){
            perfectLinkMessage.isResend = true;
        }

        // add perfectLinksMessage to msgSendMap
        MessageManager.getInstance().addMessage(perfectLinkMessage);
    }

    // deliver message
    public void indication(PerfectLinkMessage perfectLinkMessage){
        int senderId = perfectLinkMessage.senderId;
        int PSEQ = perfectLinkMessage.PSEQ;

        // deliver if not already delivered. don't exist, return null
        if(delivered.get(senderId).putIfAbsent(PSEQ, 0) == null){
            // delivered a message with sequence number from process number
            String logStr = "d " + perfectLinkMessage.senderId + " " + perfectLinkMessage.SEQ + "\n";
            // addLogBuffer(logStr);
            if(cs451.Constants.DEBUG_OUTPUT_PL){
                System.out.print("[pl]   "+logStr);
            }

            UniformReliableBroadcast.getInstance().indication(perfectLinkMessage);
        }
    }

    public int getAndIncreasePSEQ(){
       return currentPSEQ.getAndIncrement();
    }

}