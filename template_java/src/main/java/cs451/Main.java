package cs451;

import java.io.IOException;
import java.util.HashMap;

import cs451.PerfectLinks.SocketClient;
import cs451.PerfectLinks.SocketServer;
import cs451.Host;
import main.java.cs451.PerfectLinks.HostManager;
import main.java.cs451.PerfectLinks.PerfectLink;
import main.java.cs451.PerfectLinks.PerfectLinkMessage;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");

        PerfectLink.getInstance().writeLogFile();
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

        // get current process host info
        int myId = parser.myId();

        // init HostManager (Singleton)
        HostManager hostManager = HostManager.getInstance();
        Host myHost = hostManager.init(parser.hosts(), myId);

        // init PerfectLinks (Singleton)
        PerfectLink perfectLink = PerfectLink.getInstance();
        perfectLink.init(myId, myHost, parser.output());

        System.out.println("Broadcasting and delivering messages...\n");

        // Sleep 5s, wait for other process to start listening
        Thread.sleep(5 * 1000);

        // send messages according to config
        for(int[] pair : parser.getMessageConfigList()){
            // m defines how many messages each process should send.
            // i is the index of the process that should receive the messages.
            int m = pair[0];
            int i = pair[1];

            if(i == myId){
                continue;
            }

            Host desHost = hostManager.getInstance().getHostById(i);
            for(int SEQ = 0; SEQ < m; SEQ++){
                PerfectLinkMessage perfectLinkMessage = new PerfectLinkMessage(desHost, myHost, SEQ, perfectLink.getAndIncreasePSEQ());
                perfectLink.request(perfectLinkMessage);
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
