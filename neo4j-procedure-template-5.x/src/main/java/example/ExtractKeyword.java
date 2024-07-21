package example;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ExtractKeyword {

    public static String runScript(String plotText) {
        StringBuilder output = new StringBuilder();
        try {


            // Définir le chemin relatif du script Python
            String scriptPath = "/Users/mariusayrault/GitHub/Sorb-Data-Analytics/projet-sda-cloud-2/neo4j-procedure-template-5.x/src/scripts/extract_keywords.py";

            // Construire la commande
            ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath, plotText);
            processBuilder.redirectErrorStream(true);

            // Exécuter le script
            Process process = processBuilder.start();

            // Lire la sortie du script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Attendre la fin du processus
            int exitCode = process.waitFor();
            System.out.println("Process exited with code " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}
