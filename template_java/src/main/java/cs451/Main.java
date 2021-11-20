package cs451;

import main.java.cs451.fifo.FIFOBroadcast;
import main.java.cs451.fifo.FIFOMessage;
import main.java.cs451.tool.HostManager;
import main.java.cs451.tool.OutputManager;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");

        OutputManager.getInstance().writeLogFile();
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
        int configType = 1;
        Parser parser = new Parser(args, configType);
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

        // get info from parser
        int myId = parser.myId();
        String outputPath = parser.output();

        // init OutputManager
        OutputManager.getInstance().init(outputPath);

        // init HostManager (Singleton)
        HostManager hostManager = HostManager.getInstance();
        Host myHost = hostManager.init(parser.hosts(), myId);

        // init FIFOBroadcast (Singleton)
        FIFOBroadcast fifoBroadcast = FIFOBroadcast.getInstance();
        fifoBroadcast.init(myId, myHost);

        System.out.println("Broadcasting and delivering messages...\n");

        for(int m : parser.getMessageConfigList1()){
            // m defines how many messages each process should broadcast
            for(int i = 0; i < m; i++) {
                FIFOMessage fifoMessage = new FIFOMessage(myId, fifoBroadcast.getAndIncreaseFIFOSEQ());
                fifoBroadcast.request(fifoMessage);

                // pause for a while after sending some messages
                if(m % 64 == 0){
                    Thread.sleep(500);
                }
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
