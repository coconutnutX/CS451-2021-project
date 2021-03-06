package main.java.cs451.pl;

import cs451.Host;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.net.*;
import java.io.*;

public class SocketServer extends Thread{

    private int myId;
    private Host myHost;

    private DatagramSocket datagramSocket;
    private ExecutorService executor;

    public SocketServer(int myId, Host myHost){
        this.myId = myId;
        this.myHost = myHost;

        // handler thread pool
        executor = Executors.newFixedThreadPool(cs451.Constants.PL_THREADPOOL_SIZE);

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