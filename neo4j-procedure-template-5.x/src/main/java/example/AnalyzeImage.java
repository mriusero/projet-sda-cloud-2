package example;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AnalyzeImage {

    public static String runScript(String imageUrl) {
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        try {
            // Define the relative path to the Python script
            String scriptPath = ".../neo4j-procedure-template-5.x/src/scripts/analyze_image.py";

            // Build the command
            ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath, imageUrl);
            processBuilder.redirectErrorStream(true);

            // Execute the script
            Process process = processBuilder.start();

            // Read the script output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Read any potential errors
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Process exited with code " + exitCode);
                System.err.println("Error Output: " + errorOutput.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorOutput.append(e.getMessage());
        }
        return output.toString();
    }
}
