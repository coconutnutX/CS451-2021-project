package cs451.PerfectLinks;

import java.net.*;

public class SocketServerHandler extends Thread{

    private DatagramPacket datagramPacket;

    public SocketServerHandler(DatagramPacket datagramPacket){
        this.datagramPacket = datagramPacket;
    }

    public void run() {
        try {
            String msg = new String(datagramPacket.getData());
            System.out.println("Receive: [" + msg+"]");

            // TODO handle message and write to file

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
