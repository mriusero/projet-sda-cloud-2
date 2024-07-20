package example;

import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Question4 {

    @Context
    public Transaction tx;

    public static class Similarity {
        public String movieTitle;
        public long sharedGenres;

        public Similarity(String movieTitle, long sharedGenres) {
            this.movieTitle = movieTitle;
            this.sharedGenres = sharedGenres;
        }
    }

    @Procedure(name = "movie.findSimilarMovies", mode = Mode.READ)
    @Description("Find movies most similar to Inception based on shared genres.")
    public Stream<Similarity> findSimilarMovies(@Name("movieTitle") String movieTitle) {
        String query = "MATCH (targetMovie:Movie {title: $movieTitle})-[:IN_GENRE]->(genre:Genre)<-[:IN_GENRE]-(similar:Movie) " +
                "WHERE targetMovie <> similar " +
                "WITH similar, COUNT(genre) AS sharedGenres " +
                "RETURN similar.title AS movieTitle, sharedGenres " +
                "ORDER BY sharedGenres DESC";

        // Execute the query with the provided parameter
        List<Map<String, Object>> resultList = tx.execute(query, Map.of("movieTitle", movieTitle)).stream().toList();

        // Collect results into a List of Similarity
        List<Similarity> results = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            String similarMovieTitle = (String) row.get("movieTitle");
            long sharedGenres = (Long) row.get("sharedGenres");
            results.add(new Similarity(similarMovieTitle, sharedGenres));
        }

        // Return results as a stream
        return results.stream();
    }
}
