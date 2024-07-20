package example;

import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Question5 {

    @Context
    public Transaction tx;

    public static class Recommendation {
        public String movieTitle;
        public long sharedGenres;

        public Recommendation(String movieTitle, long sharedGenres) {
            this.movieTitle = movieTitle;
            this.sharedGenres = sharedGenres;
        }
    }

    @Procedure(name = "movie.recommendSimilarMovies", mode = Mode.READ)
    @Description("Recommend movies similar to those the user has already watched based on shared genres.")
    public Stream<Recommendation> recommendSimilarMovies(@Name("userName") String userName) {
        String query = "MATCH (user:User {name: $userName})-[:RATED]->(watched:Movie)-[:IN_GENRE]->(genre:Genre)<-[:IN_GENRE]-(recommend:Movie) " +
                "WHERE NOT (user)-[:RATED]->(recommend) AND watched <> recommend " +
                "WITH recommend, COUNT(genre) AS sharedGenres " +
                "RETURN recommend.title AS movieTitle, sharedGenres " +
                "ORDER BY sharedGenres DESC " +
                "LIMIT 10";

        // Execute the query with the provided parameter
        List<Map<String, Object>> resultList = tx.execute(query, Map.of("userName", userName)).stream().toList();

        // Collect results into a List of Recommendation
        List<Recommendation> results = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            String movieTitle = (String) row.get("movieTitle");
            long sharedGenres = (Long) row.get("sharedGenres");
            results.add(new Recommendation(movieTitle, sharedGenres));
        }

        // Return results as a stream
        return results.stream();
    }
}
