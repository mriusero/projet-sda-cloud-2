package example;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ExtractKeyword {

    public static String runScript(String plotText) {
        StringBuilder output = new StringBuilder();
        try {

            // Path to the python script
            String scriptPath = ".../projet-sda-cloud-2/neo4j-procedure-template-5.x/src/scripts/extract_keywords.py";

            // Build the command
            ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath, plotText);
            processBuilder.redirectErrorStream(true);

            // Execute the script
            Process process = processBuilder.start();

            // Read the script output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Process exited with code " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}
