package main.java.cs451.PerfectLinks;

import cs451.Host;

public class RequestMessage {

    private static final String SPACES_REGEX = "\\s+";

    private Host receiver;
    private Host sender;
    private int SEQ;
    private int PSEQ;

    private String message;

    // construct from sender
    public RequestMessage(Host receiver, Host sender, int SEQ, int PSEQ){
        this.receiver = receiver;
        this.sender = sender;
        this.SEQ = SEQ;
        this.PSEQ = PSEQ;

        // concatenate message string
        this.message = sender + " " + SEQ + PSEQ;
    }

    // construct from receiver
    public RequestMessage(String message){
        this.message = message;

        // parse message string
        String[] splits = message.split(SPACES_REGEX);
        int senderId = Integer.parseInt(splits[0]);
        // find host info
        this.sender = HostManager.getInstance().getHostById(senderId);
        this.SEQ = Integer.parseInt(splits[1]);
        this.PSEQ = Integer.parseInt(splits[2]);
    }

}
