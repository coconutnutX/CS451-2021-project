package cs451.PerfectLinks;

import main.java.cs451.pl.MessageManager;
import main.java.cs451.pl.PerfectLink;
import main.java.cs451.pl.PerfectLinkMessage;

import java.net.*;

public class SocketServerHandler extends Thread{

    private DatagramPacket datagramPacket;

    public SocketServerHandler(DatagramPacket datagramPacket){
        this.datagramPacket = datagramPacket;
    }

    public void run() {
        try {
            String messageString = new String(datagramPacket.getData());
            // System.out.println("Receive: [" + messageString+"]");

            // parse message
            PerfectLinkMessage perfectLinkMessage = new PerfectLinkMessage(messageString);

            if(!perfectLinkMessage.isACK){ // is message
                // send ACK
                SocketClient socketClient = new SocketClient();
                socketClient.sendACK(perfectLinkMessage);

                // call deliver
                PerfectLink.getInstance().indication(perfectLinkMessage);
            }else{ // is ACK
                // remove from track
                MessageManager.getInstance().removeMessage(perfectLinkMessage.getPSEQ());
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}