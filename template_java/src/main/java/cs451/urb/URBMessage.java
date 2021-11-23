package main.java.cs451.urb;

import java.util.Arrays;
import java.util.BitSet;

public class URBMessage {
    public int createrId;
    public int SEQ;
    public BitSet bitSet;            // acks in urb
    public String vectorClockStr;    // vector clock for lcb
    public int[] vectorClock;

    public URBMessage(int createrId, int SEQ, String vectorClockStr){
        this.createrId = createrId;
        this.SEQ = SEQ;
        this.bitSet = new BitSet();
        this.vectorClockStr = vectorClockStr;
        this.vectorClock = Arrays.stream(vectorClockStr.split("-")).mapToInt(Integer::parseInt).toArray();
    }

}
