package main.java.cs451.urb;

import java.util.BitSet;

public class URBMessage {
    public int createrId;
    public int SEQ;
    public String vectorClockStr;    // vector clock for lcb
    public BitSet bitSet;            // acks in urb

    public URBMessage(int createrId, int SEQ, String vectorClockStr){
        this.createrId = createrId;
        this.SEQ = SEQ;
        this.vectorClockStr = vectorClockStr;
        this.bitSet = new BitSet();
    }

}
