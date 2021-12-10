package main.java.cs451.lcb;

import main.java.cs451.pl.PerfectLink;
import main.java.cs451.pl.PerfectLinkMessage;
import main.java.cs451.tool.HostManager;

import java.util.HashMap;

/**
 * check if there is any legacy message, that is not depended by current messages, thus not delivered
 */
public class CheckLegacyDeliverThread extends Thread{

    public void run(){
        try {
            LocalizedCausalBroadcast localizedCausalBroadcast = LocalizedCausalBroadcast.getInstance();
            int totalHost = HostManager.getInstance().getTotalHostNumber();
            while(true){
                // sleep
                Thread.sleep( cs451.Constants.LCB_CHECK_PERIOD);

                for(int i=1; i<=totalHost; i++){
                    localizedCausalBroadcast.checkDeliver(i);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
