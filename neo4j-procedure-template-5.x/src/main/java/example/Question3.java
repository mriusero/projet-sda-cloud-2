package example;

import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Question3 {

    @Context
    public Transaction tx;

    public static class Recommendation {
        public String title;
        public long commonRatings;

        public Recommendation(String title, long commonRatings) {
            this.title = title;
            this.commonRatings = commonRatings;
        }
    }

    @Procedure(name = "recommend.collaborativeRecommendations", mode = Mode.READ)
    @Description("Recommend movies based on ratings from similar users.")
    public Stream<Recommendation> collaborativeRecommendations(@Name("userId") String userId) {
        String query = "MATCH (targetUser:User {userId: $userId})-[:RATED]->(movie:Movie)<-[:RATED]-(similarUser:User) " +
                "WHERE targetUser <> similarUser " +
                "MATCH (similarUser)-[:RATED]->(recommendedMovie:Movie) " +
                "WHERE NOT EXISTS { MATCH (targetUser)-[:RATED]->(recommendedMovie) } " +
                "RETURN recommendedMovie.title AS title, COUNT(*) AS commonRatings " +
                "ORDER BY commonRatings DESC LIMIT 10";

        // Execute the query with the provided parameter
        List<Map<String, Object>> resultList = tx.execute(query, Map.of("userId", userId)).stream().toList();

        // Collect results into a List of Recommendation
        List<Recommendation> results = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            String movieTitle = (String) row.get("title");
            long commonRatings = (Long) row.get("commonRatings");
            results.add(new Recommendation(movieTitle, commonRatings));
        }

        // Return results as a stream
        return results.stream();
    }
}
