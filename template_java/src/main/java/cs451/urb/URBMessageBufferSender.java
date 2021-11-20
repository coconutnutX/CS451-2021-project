package main.java.cs451.urb;

import cs451.Constants;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class URBMessageBufferSender extends Thread{

    private ConcurrentLinkedQueue<URBMessage> buffer;         // messages to send
    private AtomicInteger pendingNum;                         // pending message number (can not use pending.size)
    private int windowSize;                                   // send buffer sliding window size
    private int totalCount;                                   // count total send number for debug
    private int period;                                       // initial gap period
    private int round;                                        // round of checking, start delay after some number

    public URBMessageBufferSender(ConcurrentLinkedQueue<URBMessage> buffer, AtomicInteger pendingNum){
        this.buffer = buffer;
        this.pendingNum = pendingNum;
        this.windowSize = cs451.Constants.URB_BUFFERED_WINDOW_SIZE;
        this.totalCount = 0;
        this.period = cs451.Constants.INIT_URB_BUFFERED_SEND_PERIOD;
        this.round = 0;
    }

    public void run(){
        try{
            while(true){
                int count = 0;
                // check if window is not full & has message in buffer
                while(pendingNum.get() < windowSize && buffer.size() > 0){
                    // get a message from buffer and send it
                    URBMessage urbMessage = buffer.remove();
                    UniformReliableBroadcast.getInstance().request(urbMessage);
                    // add pending number
                    pendingNum.incrementAndGet();
                    count++;
                }
                totalCount += count;

                if(round > Constants.URB_BUFFERED_START_DELAY_AFTER_ROUND){
                    // if has no message in buffer, double check period
                    period = period*2 < Constants.MAX_URB_BUFFERED_SEND_PERIOD ? period*2 : Constants.MAX_URB_BUFFERED_SEND_PERIOD;
                }

                if(cs451.Constants.DEBUG_OUTPUT_URB_BUFFER){
                    System.out.println("[urb buffer] send:" + count + " total send:" + totalCount + " gap:" + period);
                }

                round++;

                // sleep
                Thread.sleep(period);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
