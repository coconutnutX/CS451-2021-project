package main.java.cs451.pl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import cs451.Host;
import main.java.cs451.pl.SocketServer;
import main.java.cs451.pl.SocketClient;
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
    private int currentPSEQ;        // keep track of PSEQ of this process, should be unique in all processes

    private Thread socketServer;    // thread to listen to sockets
    private Thread messageResender; // thread to resend sockets periodically

    private HashMap<Integer, HashSet<Integer>> deliverMap; // messages delivered, <sender ID, PSEQ>

    public static PerfectLink getInstance(){
        return instance;
    }

    public void init(int myId, Host myHost) {
        this.myId = myId;
        this.myHost = myHost;
        this.currentPSEQ = 1;
        this.deliverMap = new HashMap<Integer, HashSet<Integer>>();
        this.socketServer = new SocketServer(myId, myHost);
        this.messageResender = new MessageResender();

        // start listen to port
        socketServer.start();

        // check periodically to resend messages
        messageResender.start();

        System.out.println("PerfectLink initialized\n");
    }

    // request to send message perfectLinkMessage
    public synchronized void request(PerfectLinkMessage perfectLinkMessage){
        // log info
        if(!perfectLinkMessage.isResend){
            String logStr = "b " + perfectLinkMessage.getSEQ() + "\n";
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
    public synchronized void indication(PerfectLinkMessage perfectLinkMessage){
        int senderId = perfectLinkMessage.getSender().getId();
        int PSEQ = perfectLinkMessage.getPSEQ();

        // init set if don't exist
        if(!deliverMap.containsKey(senderId)){
            deliverMap.put(senderId, new HashSet<Integer>());
        }

        // deliver if not already delivered
        if(!deliverMap.get(senderId).contains(PSEQ)){

            deliverMap.get(senderId).add(PSEQ);

            // delivered a message with sequence number from process number
            String logStr = "d " + perfectLinkMessage.getSender().getId() + " " + perfectLinkMessage.getSEQ() + "\n";
            // addLogBuffer(logStr);
            if(cs451.Constants.DEBUG_OUTPUT_PL){
                System.out.print("[pl]   "+logStr);
            }

            UniformReliableBroadcast.getInstance().indication(perfectLinkMessage);
        }

    }

    public int getAndIncreasePSEQ(){
       return currentPSEQ++;
    }

}