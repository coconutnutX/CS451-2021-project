package cs451;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class ConfigParser {

    private static final String SPACES_REGEX = "\\s+";

    private String path;

    private List<int[]> messageConfigList = new ArrayList<>();

    /**
     *
     * The CONFIG command-line argument for this algorithm consists of a file
     * that contains two integers m i in its first line.
     * m defines how many messages each process should send.
     * i is the index of the process that should receive the messages.
     * Note that all processes, apart from i, send m messages each to process i
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

                String[] splits = line.split(SPACES_REGEX);
                if (splits.length != 2) {
                    System.err.println("Problem with the line " + lineNum + " in the config file!");
                    return false;
                }

                int[] newConfig = new int[2];
                newConfig[0] = Integer.parseInt(splits[0]);
                newConfig[1] = Integer.parseInt(splits[1]);

                messageConfigList.add(newConfig);
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

    public List<int[]> getMessageConfigList() {
        return messageConfigList;
    }

}
