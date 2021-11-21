package main.java.cs451.pl;

import cs451.Host;

import java.net.*;

public class SocketClient {

    public void sendMessage(PerfectLinkMessage perfectLinkMessage){
        DatagramSocket datagramSocket = null;
        try {
            // init
            datagramSocket = new DatagramSocket();
            InetSocketAddress address = new InetSocketAddress(perfectLinkMessage.receiverIp, perfectLinkMessage.receiverPort);
            byte[] sendData = (perfectLinkMessage.message).getBytes();

            // send
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

    public void sendACK(PerfectLinkMessage perfectLinkMessage){
        DatagramSocket datagramSocket = null;
        try {
            // init
            datagramSocket = new DatagramSocket();
            // here receiver is the original sender
            InetSocketAddress address = new InetSocketAddress(perfectLinkMessage.senderIp, perfectLinkMessage.senderPort);
            byte[] sendData = (perfectLinkMessage.getAckMessage()).getBytes();

            // send
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
