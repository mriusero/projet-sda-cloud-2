package example;

import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GetRecommendation {

    @Context
    public Transaction tx;

    public static class Recommendation {
        public String recommendedMovie;
        public long recommendationScore;

        public Recommendation(String recommendedMovie, long recommendationScore) {
            this.recommendedMovie = recommendedMovie;
            this.recommendationScore = recommendationScore;
        }
    }

    @Procedure(name = "movie.getSimilarUserRecommendations", mode = Mode.READ)
    @Description("Recommend movies based on the most similar users to the target user.")
    public Stream<Recommendation> getSimilarUserRecommendations(@Name("targetUser") String targetUser) {
        // Define the query
        String query =
                "WITH $targetUser AS targetUser " +
                        "CALL { " +
                        "  WITH targetUser " +
                        "  MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(movie:Movie) " +
                        "  WITH targetUser, COLLECT(movie) AS targetMovies, COLLECT({rating: r1.rating, timestamp: r1.timestamp}) AS targetRatings " +
                        "  MATCH (otherUser:User)-[r2:RATED]->(movie:Movie) " +
                        "  WHERE otherUser.name <> targetUser " +
                        "  WITH targetUser, otherUser, targetMovies, COLLECT(movie) AS otherMovies, COLLECT({rating: r2.rating, timestamp: r2.timestamp}) AS otherRatings " +
                        "  WITH targetUser, otherUser, targetMovies, otherMovies, " +
                        "       [m IN targetMovies WHERE m IN otherMovies] AS commonMovies " +
                        "  MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(m:Movie) " +
                        "  WHERE m IN commonMovies " +
                        "  WITH targetUser, otherUser, commonMovies, " +
                        "       COLLECT({rating: r1.rating, timestamp: r1.timestamp}) AS targetCommonRatings " +
                        "  MATCH (otherUser)-[r2:RATED]->(m:Movie) " +
                        "  WHERE m IN commonMovies " +
                        "  WITH otherUser, targetCommonRatings, " +
                        "       COLLECT({rating: r2.rating, timestamp: r2.timestamp}) AS otherCommonRatings " +
                        "  WITH otherUser, " +
                        "       gds.similarity.pearson( " +
                        "           [x IN targetCommonRatings | x.rating * EXP(-0.01 * (timestamp() - x.timestamp))], " +
                        "           [x IN otherCommonRatings | x.rating * EXP(-0.01 * (timestamp() - x.timestamp))] " +
                        "       ) AS pearsonSimilarity " +
                        "  WHERE pearsonSimilarity IS NOT NULL " +
                        "  RETURN otherUser AS similarUser, pearsonSimilarity " +
                        "  ORDER BY pearsonSimilarity DESC " +
                        "  LIMIT 10 " +
                        "} " +
                        "MATCH (similarUser)-[r:RATED]->(movie:Movie) " +
                        "WHERE NOT EXISTS { " +
                        "  MATCH (targetUserNode:User {name: targetUser})-[r2:RATED]->(movie) " +
                        "} " +
                        "RETURN movie.title AS recommendedMovie, COUNT(similarUser) AS recommendationScore " +
                        "ORDER BY recommendationScore DESC " +
                        "LIMIT 10";

        // Execute the query with the provided parameter
        List<Map<String, Object>> resultList = tx.execute(query, Map.of("targetUser", targetUser)).stream().toList();

        // Collect results into a List of Recommendation
        List<Recommendation> results = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            String recommendedMovie = (String) row.get("recommendedMovie");
            long recommendationScore = (Long) row.get("recommendationScore");
            results.add(new Recommendation(recommendedMovie, recommendationScore));
        }

        // Return results as a stream
        return results.stream();
    }
}
