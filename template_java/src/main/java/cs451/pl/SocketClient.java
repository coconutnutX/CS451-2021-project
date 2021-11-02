package cs451.PerfectLinks;

import cs451.Host;
import main.java.cs451.pl.PerfectLink;
import main.java.cs451.pl.PerfectLinkMessage;

import java.net.*;

public class SocketClient {

    private static DatagramSocket datagramSocket;

    public void sendMessage(PerfectLinkMessage perfectLinkMessage){
        try {
            // init
            DatagramSocket datagramSocket = new DatagramSocket();
            Host receiver = perfectLinkMessage.getReceiver();
            InetSocketAddress address = new InetSocketAddress(receiver.getIp(), receiver.getPort());
            byte[] sendData = (perfectLinkMessage.getMessage()).getBytes();

            // send
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address);
            datagramSocket.send(sendPacket);

            // System.out.println("Send to "+receiver.getId()+": ["+perfectLinkMessage.getMessage()+"]");
            // broadcast a message with sequence number 3
            if(!perfectLinkMessage.isResend){
                String logStr = "b " + perfectLinkMessage.getSEQ() + "\n";
                PerfectLink.getInstance().addLogBuffer(logStr);
                // System.out.print(logStr);
            }
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
        try {
            // init
            DatagramSocket datagramSocket = new DatagramSocket();
            Host host = perfectLinkMessage.getSender(); // here receiver is the original sender
            InetSocketAddress address = new InetSocketAddress(host.getIp(), host.getPort());
            byte[] sendData = (perfectLinkMessage.getAckMessage()).getBytes();

            // send
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address);
            datagramSocket.send(sendPacket);

            // System.out.println("Send ACK to "+host.getId()+": ["+perfectLinkMessage.getAckMessage()+"]");
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