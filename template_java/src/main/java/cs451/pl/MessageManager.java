package main.java.cs451.pl;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Keep track of all messages send but not yet received ACK
 * Check regularly to resend
 * Singleton
 */
public class MessageManager {

    private static MessageManager instance = new MessageManager();
    private ConcurrentHashMap<Integer, PerfectLinkMessage> messageSendMap; // index:PSEQ

    private MessageManager(){
        messageSendMap = new ConcurrentHashMap<Integer, PerfectLinkMessage>();
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

    public ConcurrentHashMap<Integer, PerfectLinkMessage> getMessageSendMap(){
        return messageSendMap;
    }
}
