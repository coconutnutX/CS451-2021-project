package main.java.cs451.PerfectLinks;

import java.util.HashMap;

/**
 * Keep track of all messages send but not yet received ACK
 * Check regularly to resend
 * Singleton
 */
public class MessageManager {

    private static MessageManager instance = new MessageManager();
    private HashMap<Integer, PerfectLinkMessage> messageSendMap; // index:PSEQ

    private MessageManager(){
        messageSendMap = new HashMap<Integer, PerfectLinkMessage>();
    }

    public static MessageManager getInstance(){
        return instance;
    }

    public void addMessage(PerfectLinkMessage perfectLinkMessage){
        messageSendMap.put(perfectLinkMessage.getPSEQ(), perfectLinkMessage);
    }

    public void removeMessage(int PSEQ){
        messageSendMap.remove(PSEQ);
    }

    public HashMap<Integer, PerfectLinkMessage> getMessageSendMap(){
        return messageSendMap;
    }
}
