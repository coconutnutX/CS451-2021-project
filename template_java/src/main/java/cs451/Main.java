package cs451;

import main.java.cs451.fifo.FIFOBroadcast;
import main.java.cs451.lcb.LocalizedCausalBroadcast;
import main.java.cs451.tool.ClientThread;
import main.java.cs451.tool.HostManager;
import main.java.cs451.tool.OutputManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
        int configType = 2;
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

        // parse config
        List<String[]> configs = parser.getMessageConfigList2();
        int m = Integer.parseInt(configs.get(0)[0]);
        System.out.println("Messages to broadcast: "+m);

        // parse dependency config
        int totalHost = hostManager.getTotalHostNumber();
//        boolean[] depend = new boolean[totalHost+1];
//        for(int i=1; i<dependencyConfig.length; i++){
//            int id = Integer.parseInt(dependencyConfig[i]);
//            depend[id] = true;
//        }

        // parse all dependency
        HashMap<Integer, boolean[]> hostDependency = new HashMap<>();
        for(int i=1; i<= totalHost; i++){
            boolean[] depend = new boolean[totalHost+1];
            hostDependency.put(i, depend);

            String[] dependencyConfig = configs.get(i);
            for(int j=1; j<dependencyConfig.length; j++){
                int id = Integer.parseInt(dependencyConfig[j]);
                depend[id] = true;
            }
            System.out.println("Dependency of "+i+": "+ Arrays.toString(depend));
        }

        // init LocalizedCausalBroadcast (Singleton)
        LocalizedCausalBroadcast localizedCausalBroadcast = LocalizedCausalBroadcast.getInstance();
        localizedCausalBroadcast.init(myId, myHost, hostDependency.get(myId), hostDependency);

        System.out.println("Broadcasting and delivering messages...\n");

        ClientThread clientThread = new ClientThread(myId, m);
        clientThread.run();

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
