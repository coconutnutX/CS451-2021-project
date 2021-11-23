package main.java.cs451.tool;

import main.java.cs451.lcb.LocalizedCausalBroadcast;

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
                localizedCausalBroadcast.request(myId, localizedCausalBroadcast.getAndIncreaseSEQ());
                int randomNum = ThreadLocalRandom.current().nextInt(500, 2001);
                Thread.sleep(randomNum);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
