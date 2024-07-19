package example;

import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Question2 {

    @Context
    public Transaction tx;

    public static class Recommendation {
        public String title;
        public long commonGenres;

        public Recommendation(String title, long commonGenres) {
            this.title = title;
            this.commonGenres = commonGenres;
        }
    }

    @Procedure(name = "recommend.similarMoviesByGenre", mode = Mode.READ)
    @Description("Find movies similar to the given movie based on common genres.")
    public Stream<Recommendation> similarMoviesByGenre(@Name("title") String title) {
        String query = "MATCH (movie:Movie {title: $title})-[:IN_GENRE]->(genre)<-[:IN_GENRE]-(similar:Movie) " +
                "WHERE movie <> similar " +
                "RETURN similar.title AS title, count(genre) AS commonGenres " +
                "ORDER BY commonGenres DESC LIMIT 10";

        // Execute the query with the provided parameter
        List<Map<String, Object>> resultList = tx.execute(query, Map.of("title", title)).stream().toList();

        // Collect results into a List of Recommendation
        List<Recommendation> results = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            String movieTitle = (String) row.get("title");
            long commonGenres = (Long) row.get("commonGenres");
            results.add(new Recommendation(movieTitle, commonGenres));
        }

        // Return results as a stream
        return results.stream();
    }
}
