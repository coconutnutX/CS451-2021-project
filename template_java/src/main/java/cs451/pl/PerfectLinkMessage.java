package main.java.cs451.pl;

import cs451.Host;
import main.java.cs451.tool.HostManager;

/**
 * message format: 0 senderID SEQ PSEQ
 * ACK format:     1 PSEQ
 */
public class PerfectLinkMessage {

    private static final String SPACES_REGEX = "\\s+";

    public boolean isACK;
    public boolean isResend;

    public int SEQ;
    public int PSEQ;

    public int createrId;
    public int senderId;
    public String senderIp;
    public int senderPort;
    public String receiverIp;
    public int receiverPort;
    public String vectocClockStr;    // for localized causal broadcast

    public String message;

    // construct from sender
    public PerfectLinkMessage(Host receiver, int createrId, Host sender, int SEQ, int PSEQ, String vevtorClockStr){
        this.isACK = false;
        this.isResend = false;
        this.receiverIp = receiver.getIp();
        this.receiverPort = receiver.getPort();
        this.senderId = sender.getId();
        this.createrId = createrId;
        this.SEQ = SEQ;
        this.PSEQ = PSEQ;

        this.vectocClockStr = vevtorClockStr;

        // concatenate message string
        this.message = "0 " + createrId + " " + senderId + " " + SEQ + " " + PSEQ + " " + vevtorClockStr;
    }

    // construct from receiver
    public PerfectLinkMessage(String message){
        this.message = message;

        // parse message string
        String[] splits = message.split(SPACES_REGEX);

        if(Integer.parseInt(splits[0]) == 0){
            // when receive message
            this.isACK = false;

            // find host info
            this.createrId = Integer.parseInt(splits[1]);
            this.senderId = Integer.parseInt(splits[2]);
            Host sender = HostManager.getInstance().getHostById(senderId);
            this.senderIp = sender.getIp();
            this.senderPort = sender.getPort();
            this.SEQ = Integer.parseInt(splits[3]);
            this.PSEQ = Integer.parseInt(splits[4]);
            this.vectocClockStr = splits[5].trim();        // have to trim at the end to avoid parsing error
        }else{
            // when receive ACK
            this.isACK = true;

            this.PSEQ = Integer.parseInt(splits[1].trim());
        }

    }

    // construct ACK message
    public String getAckMessage(){
        return "1 " + PSEQ;
    }

}
