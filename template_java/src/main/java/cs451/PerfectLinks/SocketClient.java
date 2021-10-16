package cs451.PerfectLinks;

import main.java.cs451.PerfectLinks.PerfectLink;
import main.java.cs451.PerfectLinks.PerfectLinkMessage;

import java.net.*;

public class SocketClient {

    private static DatagramSocket datagramSocket;

    public void sendMessage(PerfectLinkMessage perfectLinkMessage){
        try {
            // init
            DatagramSocket datagramSocket = new DatagramSocket();
            InetSocketAddress address = new InetSocketAddress(perfectLinkMessage.getDesIp(), perfectLinkMessage.getDesPort());
            byte[] sendData = (perfectLinkMessage.getMessage()).getBytes();

            // send
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address);
            datagramSocket.send(sendPacket);

            System.out.println("Send to "+perfectLinkMessage.getDesId()+": ["+perfectLinkMessage.getMessage()+"]");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally{
            if(datagramSocket != null){
                datagramSocket.close();
            }
        }
    }

}
