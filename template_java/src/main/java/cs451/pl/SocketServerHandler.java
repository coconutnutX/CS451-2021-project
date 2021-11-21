package main.java.cs451.pl;

import main.java.cs451.pl.MessageManager;
import main.java.cs451.pl.PerfectLink;
import main.java.cs451.pl.PerfectLinkMessage;
import main.java.cs451.pl.SocketClient;

import java.net.*;

public class SocketServerHandler extends Thread{

    private DatagramPacket datagramPacket;

    public SocketServerHandler(DatagramPacket datagramPacket){
        this.datagramPacket = datagramPacket;
    }

    public void run() {
        try {
            String messageString = new String(datagramPacket.getData());

            // parse message
            PerfectLinkMessage perfectLinkMessage = new PerfectLinkMessage(messageString);

            if(!perfectLinkMessage.isACK){ // is message
                // send ACK
                SocketClient socketClient = new SocketClient();
                socketClient.sendACK(perfectLinkMessage);

                PerfectLink.getInstance().indication(perfectLinkMessage);
            }else{ // is ACK
                // remove from track
                MessageManager.getInstance().removeMessage(perfectLinkMessage.PSEQ);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
