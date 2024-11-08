package example;

import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;
import java.util.stream.Stream;

public class AnalyzeImageProcedure {

    @Context
    public Transaction tx;

    @Procedure(name = "movie.AnalyzeImage", mode = Mode.WRITE)
    @Description("Call the Java class that runs a Python script and returns the result.")
    public Stream<Result> AnalyzeImage(@Name("imageUrl") String imageUrl) {
        try {

            String scriptOutput = AnalyzeImage.runScript(imageUrl);
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
