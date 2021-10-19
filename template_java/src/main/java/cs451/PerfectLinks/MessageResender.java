package main.java.cs451.PerfectLinks;

import cs451.Constants;

import java.util.HashMap;

public class MessageResender extends Thread{

    private MessageManager messageManager = MessageManager.getInstance();
    private PerfectLink perfectLink = PerfectLink.getInstance();

    public void run(){
        try {
            while(true){
                // deep copy map from messageManager, avoid java.util.ConcurrentModificationException
                HashMap<Integer, PerfectLinkMessage> copyMap = new HashMap<>();
                copyMap.putAll(messageManager.getMessageSendMap());

                // check for sockets without ACK
                for(PerfectLinkMessage perfectLinkMessage: copyMap.values()){
                    // resend
                    PerfectLink.getInstance().request(perfectLinkMessage);
                }

                // sleep
                Thread.sleep( Constants.RESEND_PERIOD);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
