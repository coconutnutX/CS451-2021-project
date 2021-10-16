package main.java.cs451.PerfectLinks;

import cs451.Constants;

import java.util.HashMap;

public class MessageResender extends Thread{

    private MessageManager messageManager = MessageManager.getInstance();
    private PerfectLink perfectLink = PerfectLink.getInstance();

    public void run(){
        try {
            while(true){
                // check for sockets without ACK
                for(PerfectLinkMessage perfectLinkMessage: messageManager.getMessageSendMap().values()){
                    // resend
                    PerfectLink.getInstance().request(perfectLinkMessage);
                }

                // TODO what if message removed while checking?

                // sleep
                Thread.sleep( Constants.RESEND_PERIOD * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
