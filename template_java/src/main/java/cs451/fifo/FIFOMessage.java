package main.java.cs451.fifo;

public class FIFOMessage {
    private int createrId;
    private int SEQ;

    public FIFOMessage(int createrId, int SEQ){
        this.createrId = createrId;
        this.SEQ = SEQ;
    }

    public int getCreaterId() {
        return createrId;
    }

    public int getSEQ() {
        return SEQ;
    }
}
