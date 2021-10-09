package cs451;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

import cs451.PerfectLinks.LinkClient;
import cs451.PerfectLinks.LinkServer;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        // store host info in HashMap
        HashMap<Integer, Host> hostMap = new HashMap<Integer, Host>();
        for (Host host: parser.hosts()) {
            hostMap.put(host.getId(), host);
        }

        // get current socket info
        int myId = parser.myId();
        Host myHost = hostMap.get(myId);

        // listen to port
        Thread linkServer = new LinkServer(parser.myId(), myHost);
        linkServer.start();

        System.out.println("Broadcasting and delivering messages...\n");

        // Sleep 10s, wait for other process to start listening
        Thread.sleep(10 * 1000);

        // send messages
        LinkClient linkClient = new LinkClient();

        for(int[] pair : parser.getMessageConfigList()){
            // m defines how many messages each process should send.
            // i is the index of the process that should receive the messages.
            int m = pair[0];
            int i = pair[1];
            System.out.println("m=" + m + " i=" + i);

            if(i == myId){
                continue;
            }

            // TODO : Does the client send each message using a new socket?

            Host desHost = hostMap.get(i);
            for(int j = 0; j < m; j++){
                String message = "TEST MESSAGE FROM ID:" + myId + " SEQ:" + (j+1);
                linkClient.sendMessage(desHost.getIp(), desHost.getPort(), message);
            }
        }

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
