package main.java.cs451.urb;

import cs451.Constants;
import main.java.cs451.fifo.FIFOBroadcast;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class URBMessageBufferSender extends Thread{

    private ConcurrentLinkedQueue<URBMessage> buffer;         // messages to send
    private AtomicInteger pendingNum;                         // pending message number (can not use pending.size)
    private int windowSize;                                   // send buffer sliding window size
    private int totalCount;                                   // count total send number for debug
    private int period;                                       // initial gap period
    private UniformReliableBroadcast uniformReliableBroadcast;

    public URBMessageBufferSender(ConcurrentLinkedQueue<URBMessage> buffer, AtomicInteger pendingNum){
        this.buffer = buffer;
        this.pendingNum = pendingNum;
        this.windowSize = cs451.Constants.URB_BUFFERED_WINDOW_SIZE;
        this.totalCount = 0;
        this.period = cs451.Constants.URB_BUFFERED_SEND_PERIOD;
        this.uniformReliableBroadcast = UniformReliableBroadcast.getInstance();
    }

    public void run(){
        try{
            while(true){
                int pending = uniformReliableBroadcast.getSelfPendingNum();
                int canSend = windowSize - pending;
                if(buffer.size() > 0 && canSend > 0){
                    canSend = canSend > buffer.size() ? buffer.size() : canSend;

                    for(int i=0; i<canSend; i++){
                        // get a message from buffer and send it
                        URBMessage urbMessage = buffer.remove();
                        UniformReliableBroadcast.getInstance().request(urbMessage);
                        // add pending number
                        // pendingNum.incrementAndGet();
                        totalCount++;
                    }
                }
//                int pending = pendingNum.get();
//                // check if window is not full & has message in buffer
//                while(pendingNum.get() < windowSize && buffer.size() > 0){
//                    // get a message from buffer and send it
//                    URBMessage urbMessage = buffer.remove();
//                    UniformReliableBroadcast.getInstance().request(urbMessage);
//                    // add pending number
//                    pendingNum.incrementAndGet();
//                    totalCount++;
//                }

                if(cs451.Constants.DEBUG_OUTPUT_URB_BUFFER){
                    System.out.println("[urb buffer] total send:" + totalCount +" pending:"+pending+" [ud]"+UniformReliableBroadcast.getInstance().getSelfDeliveredNum()+" [fd]"+FIFOBroadcast.getInstance().getSelfDeliveredNum());
                }

                // sleep
                Thread.sleep(period);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
