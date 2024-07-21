package example;

import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;
import java.util.stream.Stream;

public class ExtractKeywordProcedure {

    @Context
    public Transaction tx;

    @Procedure(name = "movie.ExtractKeyword", mode = Mode.WRITE)
    @Description("Call the Java class that runs a Python script and returns the result.")
    public Stream<Result> ExtractKeyword(@Name("plotText") String plotText) {
        try {
            // Appeler la méthode de la classe RunPythonScript
            String scriptOutput = ExtractKeyword.runScript(plotText);
            return Stream.of(new Result(scriptOutput));
        } catch (Exception e) {
            return Stream.of(new Result("Error: " + e.getMessage()));
        }
    }

    public static class Result {
        public String message;

        public Result(String message) {
            this.message = message;
        }
    }
}
