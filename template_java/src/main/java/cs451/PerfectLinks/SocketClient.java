package cs451.PerfectLinks;

import java.net.*;
import java.io.*;

public class SocketClient {

    public SocketClient(){
    }

    /***
     *
     * broadcast of application message, using the format bseq_nr,
     * where seq_nr is the sequence number of the message
     *
     * Even though messages are not being broadcast,
     * processes that send messages log them using the format b seq_nr
     */
    public void sendMessage(String desIp, int desPort, String message){
        System.out.println("Send message to port:" + desPort);

        try {
            Socket socket = new Socket(desIp, desPort);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF(message);
            socket.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

}
