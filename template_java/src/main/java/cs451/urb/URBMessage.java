package main.java.cs451.urb;

import java.util.HashSet;
import java.util.Set;

public class URBMessage {
    private int createrId;
    private int SEQ;
    private Set<Integer> ackSet; // store sender id of pl message

    public URBMessage(int createrId, int SEQ){
        this.createrId = createrId;
        this.SEQ = SEQ;
        this.ackSet = new HashSet<>();
    }

    public void addAck(int senderId){
        ackSet.add(senderId);
    }

    public int ackNumber(){
        return ackSet.size();
    }

    public int getSEQ(){
        return SEQ;
    }

    public int getCreaterId(){
        return createrId;
    }

}