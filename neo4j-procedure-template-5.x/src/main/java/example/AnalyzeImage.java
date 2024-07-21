package example;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AnalyzeImage {

    public static String runScript(String imageUrl) {
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        try {
            // Définir le chemin relatif du script Python
            String scriptPath = "/src/scripts/analyze_image.py";

            // Construire la commande
            ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath, imageUrl);
            processBuilder.redirectErrorStream(true);

            // Exécuter le script
            Process process = processBuilder.start();

            // Lire la sortie du script
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Lire les erreurs éventuelles
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }

            // Attendre la fin du processus
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
