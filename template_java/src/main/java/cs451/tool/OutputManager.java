package main.java.cs451.tool;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class OutputManager {

    private static OutputManager instance = new OutputManager();
    private OutputManager(){}

    private StringBuffer logBuffer; // store log, write to file when terminate
    private String outputPath;      // output log file path

    public static OutputManager getInstance(){
        return instance;
    }
    public void init(String outputPath){
        this.outputPath = outputPath;
    }

    public void addLogBuffer(String str){
        logBuffer.append(str);
    }

    public void writeLogFile(){
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath));
            System.out.println(logBuffer.toString());
            bufferedWriter.write(logBuffer.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
