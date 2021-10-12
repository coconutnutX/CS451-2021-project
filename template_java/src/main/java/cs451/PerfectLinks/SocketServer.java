package cs451.PerfectLinks;

import cs451.Host;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.net.*;
import java.io.*;

import cs451.PerfectLinks.SocketServerHandler;

public class SocketServer extends Thread{

    private int myId;
    private Host myHost;

    private ServerSocket serverSocket;
    private ExecutorService executor;

    public SocketServer(int myId, Host myHost){
        this.myId = myId;
        this.myHost = myHost;

        executor = Executors.newFixedThreadPool(2);

        System.out.println("init server`~~~~~~~~~~~~`");

        // init ServerSocket
        try {
            serverSocket = new ServerSocket(myHost.getPort());
            serverSocket.setSoTimeout(10000);
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            Socket client = null;
            while(true){
                // wait for client
                client = serverSocket.accept();

                // use thread pool to handle client message
                executor.execute(new SocketServerHandler(client));
            }
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }

}