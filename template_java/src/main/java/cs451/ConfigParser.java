package cs451;

import javax.lang.model.type.ArrayType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class ConfigParser {

    private static final String SPACES_REGEX = "\\s+";
    private int configType;

    private String path;

    private List<int[]> messageConfigList0 = new ArrayList<>();
    private List<Integer> messageConfigList1 = new ArrayList<>();
    private List<String[]> messageConfigList2 = new ArrayList<>();

    public ConfigParser(int configType){
        this.configType = configType;
    }


    /**
     *
     * The CONFIG command-line argument for this algorithm consists of a file
     *
     * type 0 contains two integers m i in its first line.
     * m defines how many messages each process should send.
     * i is the index of the process that should receive the messages.
     * Note that all processes, apart from i, send m messages each to process i
     *
     * type 1 contains an integer m in its first line.
     * m defines how many messages each process should broadcast.
     */
    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();

        try(BufferedReader br = new BufferedReader(new FileReader(value))) {
            int lineNum = 1;
            for(String line; (line = br.readLine()) != null; lineNum++) {
                if (line.isBlank()) {
                    continue;
                }

                if(configType == 0){
                    // config 0: Perfect Links application
                    String[] splits = line.split(SPACES_REGEX);
                    if (splits.length != 2) {
                        System.err.println("Problem with the line " + lineNum + " in the config file!");
                        return false;
                    }

                    int[] newConfig = new int[2];
                    newConfig[0] = Integer.parseInt(splits[0]);
                    newConfig[1] = Integer.parseInt(splits[1]);

                    messageConfigList0.add(newConfig);

                }else if(configType == 1){
                    // config 1: FIFO Broadcast application
                    int newConfig = Integer.parseInt(line);
                    messageConfigList1.add(newConfig);
                }else{
                    // config 2: Localized Causal Broadcast
                    String[] splits = line.split(SPACES_REGEX);
                    messageConfigList2.add(splits);
                }


            }
        } catch (IOException e) {
            System.err.println("Problem with the config file!");
            return false;
        }


        return true;
    }

    public String getPath() {
        return path;
    }

    public List<int[]> getMessageConfigList0() {
        return messageConfigList0;
    }

    public List<Integer> getMessageConfigList1() {
        return messageConfigList1;
    }

    public List<String[]> getMessageConfigList2() {
        return messageConfigList2;
    }

}
