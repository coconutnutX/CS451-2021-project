package cs451.PerfectLinks;

import cs451.Host;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.net.*;
import java.io.*;

import cs451.PerfectLinks.SocketServerHandler;
import main.java.cs451.PerfectLinks.PerfectLink;

public class SocketServer extends Thread{

    private int myId;
    private Host myHost;

    private DatagramSocket datagramSocket;
    private ExecutorService executor;

    public SocketServer(int myId, Host myHost){
        this.myId = myId;
        this.myHost = myHost;

        // handler thread pool
        executor = Executors.newFixedThreadPool(4);

        try {
            datagramSocket = new DatagramSocket(myHost.getPort());
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            while(true){
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // wait for client
                datagramSocket.receive(receivePacket);

                // use thread pool to handle client socket packet
                executor.execute(new SocketServerHandler(receivePacket));
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        finally {
            datagramSocket.close();
        }
    }

}