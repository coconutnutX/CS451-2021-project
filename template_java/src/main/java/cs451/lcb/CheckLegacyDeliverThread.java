package main.java.cs451.lcb;

import main.java.cs451.pl.PerfectLink;
import main.java.cs451.pl.PerfectLinkMessage;

import java.util.HashMap;

/**
 * check if there is any legacy message, that is not depended by current messages, thus not delivered
 */
public class CheckLegacyDeliverThread extends Thread{

    public void run(){
        try {
            while(true){


                // sleep
                Thread.sleep( cs451.Constants.RESEND_PERIOD);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
