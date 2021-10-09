package cs451.PerfectLinks;

import java.net.*;
import java.io.*;

public class LinkServerHandler extends Thread{

    private Socket client;
    private DataInputStream in;

    public LinkServerHandler(Socket client){
        this.client = client;

        try {
            in = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Handler thread created, client address: " + client.getRemoteSocketAddress() + "\n");
    }

    /**
     * delivery of application message, using the format dsender seq_nr,
     * where sender is the number of the process that broadcast the message
     * and seq_nr is the sequence number of the message (as numbered by the broadcasting process)
     *
     * process i logs the messages using the format d sender seq_nr
     */
    public void run() {
        try {
            String msg = in.readUTF();
            System.out.println("Message from client: " + msg);

            // TODO handle message and write to file

            // close server
            client.close();
        }catch(SocketTimeoutException s) {
            System.out.println("Socket time out.\n");
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

}
