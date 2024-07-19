package example;

import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Question1 {

	@Context
	public Transaction tx;

	public static class EntityContainer {
		public String title;
		public long reviews;

		public EntityContainer(String title, long reviews) {
			this.title = title;
			this.reviews = reviews;
		}
	}

	@Procedure(name = "recommend.howManyReview", mode = Mode.READ)
	public Stream<EntityContainer> howManyReview() {
		String query = "MATCH (m:Movie)<-[:RATED]-(u:User) " +
				"WHERE m.title CONTAINS 'Matrix' " +
				"WITH m, count(*) AS reviews " +
				"RETURN m.title AS title, reviews " +
				"ORDER BY reviews DESC LIMIT 5";

		// Execute the query and get the result as a Map
		List<Map<String, Object>> resultList = tx.execute(query).stream().toList();

		// Collect results into a List of EntityContainer
		List<EntityContainer> results = new ArrayList<>();
		for (Map<String, Object> row : resultList) {
			String title = (String) row.get("title");
			long reviews = (Long) row.get("reviews");
			results.add(new EntityContainer(title, reviews));
		}

		// Return results as a stream
		return results.stream();
	}
}
