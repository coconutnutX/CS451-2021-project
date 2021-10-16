package main.java.cs451.PerfectLinks;

import cs451.Host;

public class PerfectLinkMessage {

    private static final String SPACES_REGEX = "\\s+";

    private int SEQ;
    private int PSEQ;

    private Host receiver;
    private Host sender;

    private String message;

    // construct from sender
    public PerfectLinkMessage(Host receiver, Host sender, int SEQ, int PSEQ){
        this.receiver = receiver;
        this.sender = sender;
        this.SEQ = SEQ;
        this.PSEQ = PSEQ;

        // concatenate message string
        this.message = sender.getId() + " " + SEQ + " " + PSEQ;
    }

    // construct from receiver
    public PerfectLinkMessage(String message){
        this.message = message;

        // parse message string
        String[] splits = message.split(SPACES_REGEX);
        int senderId = Integer.parseInt(splits[0]);
        // find host info
        this.sender = HostManager.getInstance().getHostById(senderId);
        this.SEQ = Integer.parseInt(splits[1]);
        this.PSEQ = Integer.parseInt(splits[2]);
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

    public int getDesId(){
        return receiver.getId();
    }

    public String getDesIp(){
        return receiver.getIp();
    }

    public int getDesPort(){
        return receiver.getPort();
    }

    public String getMessage(){
        return message;
    }

}
