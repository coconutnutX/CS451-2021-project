package cs451.PerfectLinks;

import java.net.*;
import java.io.*;

public class SocketClient {

    private static DatagramSocket datagramSocket;

    public void sendMessage(String desIp, int desPort, String message){
        System.out.println("Send message to port:" + desPort);

        try {
            datagramSocket = new DatagramSocket();
            InetSocketAddress address = new InetSocketAddress(desIp, desPort);
            byte[] sendData = (message).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address);
            datagramSocket.send(sendPacket);
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
