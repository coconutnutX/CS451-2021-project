package main.java.cs451.PerfectLinks;

import java.util.HashMap;

import cs451.Host;
import cs451.PerfectLinks.SocketServer;
import cs451.PerfectLinks.SocketClient;

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
 */
public class PerfectLink {

    private int myId;          // id of current process
    private Host myHost;       // host of current process
    private int currentPSEQ;   // keep track of PSEQ of this process, should be unique in all processes

    private HashMap<Integer, PerfectLinkMessage> msgSendMap; // messages sent but have not yet received ACK

    private Thread socketServer;   // Another thread to listen to sockets

    public PerfectLink(int myId, Host myHost) {
        this.myId = myId;
        this.myHost = myHost;
        this.currentPSEQ = 0;
        this.msgSendMap = new HashMap<Integer, PerfectLinkMessage>();
        this.socketServer = new SocketServer(myId, myHost);

        // start listen to port
        socketServer.start();

        System.out.println("PerfectLink initialized\n");
    }

    // request to send message perfectLinkMessage
    public void request(PerfectLinkMessage perfectLinkMessage){
        // send message
        SocketClient socketClient = new SocketClient();
        socketClient.sendMessage(perfectLinkMessage);

        // add perfectLinksMessage to msgSendMap
        msgSendMap.put(perfectLinkMessage.getPSEQ(), perfectLinkMessage);
    }

    public void indication(){

    }

    public int getAndIncreasePSEQ(){
       return currentPSEQ++;
    }

}