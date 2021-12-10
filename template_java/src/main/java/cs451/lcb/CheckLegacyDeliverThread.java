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
                boolean flag = false;
                for(int i=1; i<=totalHost; i++){
                    if(localizedCausalBroadcast.checkDeliver(i)){
                        flag = true;
                    }
                }
                System.out.println("check deliver, "+flag);

                // sleep
                Thread.sleep( cs451.Constants.LCB_CHECK_PERIOD);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
