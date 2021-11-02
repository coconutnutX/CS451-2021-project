package main.java.cs451.pl;

import cs451.Host;

/**
 * message format: 0 senderID SEQ PSEQ
 * ACK format:     1 PSEQ
 */
public class PerfectLinkMessage {

    private static final String SPACES_REGEX = "\\s+";

    public boolean isACK;
    public boolean isResend;

    private int SEQ;
    private int PSEQ;

    private Host receiver;
    private Host sender;

    private String message;

    // construct from sender
    public PerfectLinkMessage(Host receiver, Host sender, int SEQ, int PSEQ){
        this.isACK = false;
        this.isResend = false;
        this.receiver = receiver;
        this.sender = sender;
        this.SEQ = SEQ;
        this.PSEQ = PSEQ;

        // concatenate message string
        this.message = "0 " + sender.getId() + " " + SEQ + " " + PSEQ;
    }

    // construct from receiver
    public PerfectLinkMessage(String message){
        this.message = message;

        // parse message string
        String[] splits = message.split(SPACES_REGEX);

        if(Integer.parseInt(splits[0]) == 0){
            // when receive message
            this.isACK = false;

            int senderId = Integer.parseInt(splits[1]);
            // find host info
            this.sender = HostManager.getInstance().getHostById(senderId);
            this.SEQ = Integer.parseInt(splits[2]);
            this.PSEQ = Integer.parseInt(splits[3].trim()); // have to trim at the end to avoid parsing error
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

    public void setSEQ(int SEQ){
        this.SEQ = SEQ;
    }

    public void setPSEQ(int PSEQ){
        this.PSEQ = PSEQ;
    }

    public int getSEQ(){
        return SEQ;
    }

    public int getPSEQ(){
        return PSEQ;
    }

    public String getMessage(){
        return message;
    }

    public Host getReceiver(){
        return receiver;
    }

    public Host getSender(){
        return sender;
    }

}
