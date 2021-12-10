package main.java.cs451.tool;

import main.java.cs451.lcb.LocalizedCausalBroadcast;
import main.java.cs451.urb.UniformReliableBroadcast;

import java.util.concurrent.ThreadLocalRandom;

public class ClientThread extends Thread{

    private int myId;
    private int m;
    private LocalizedCausalBroadcast localizedCausalBroadcast;

    public ClientThread(int myId, int m){
        this.myId = myId;
        this.m = m;
        this.localizedCausalBroadcast = LocalizedCausalBroadcast.getInstance();
    }

    public void run(){
        try {
            for(int i = 0; i < m; i++) {
                int pending = localizedCausalBroadcast.getPendingNum();
                if(pending < cs451.Constants.LCB_MSG_THRESHOLD){
                    // send messsage normally
                    localizedCausalBroadcast.request(myId, localizedCausalBroadcast.getAndIncreaseSEQ());
                }else{
                    // don't send
                    i--;
                    // System.out.println("pending="+pending);
                    Thread.sleep(500);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
