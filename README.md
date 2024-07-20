# projet-sda-cloud-2
*Personalized Product Recommendations with Neo4j*
## Datasource
    SHOW PROCEDURES YIELD name WHERE name = 'apoc.cypher.runFile'

    CALL apoc.cypher.runFile('data/all-plain.cypher')

## First Query
### 1) Number of reviews for each Matrix movie
#### CYPHER
     MATCH (m:Movie)<-[:RATED]-(u:User)
     WHERE m.title CONTAINS 'Matrix'
     WITH m, count(*) AS reviews
     RETURN m.title AS movie, reviews
     ORDER BY reviews DESC LIMIT 5;
#### PROC 
    CALL recommend.howManyReview()
    YIELD title, reviews
    RETURN title, reviews

## Content-Based Filtering
### 2) Find Items similar to the item you’re looking at now
#### CYPHER
    :param title => 'Hamlet'

    MATCH (movie:Movie {title: $title})-[:IN_GENRE]->(genre)<-[:IN_GENRE]-(similar:Movie)
    WHERE movie <> similar
    RETURN similar.title AS title, count(genre) AS commonGenres
    ORDER BY commonGenres DESC
    LIMIT 25
#### PROC 
    CALL recommend.similarMoviesByGenre('Hamlet')
    YIELD title, commonGenres
    RETURN title, commonGenres

## Collaborative Filtering
### 3) Get Users who got this item, also got that other item
#### CYPHER
    // Cette requête recommande des films que les utilisateurs similaires ont appréciés mais que l'utilisateur cible n'a pas encore notés.
    
    MATCH (targetUser:User {userId: '1'})-[:RATED]->(movie:Movie)<-[:RATED]-(similarUser:User)
    WHERE targetUser <> similarUser
    MATCH (similarUser)-[:RATED]->(recommendedMovie:Movie)
    WHERE NOT EXISTS {
    MATCH (targetUser)-[:RATED]->(recommendedMovie)
    }
    RETURN recommendedMovie.title AS title, COUNT(*) AS commonRatings
    ORDER BY commonRatings DESC
    LIMIT 10
#### PROC 
    CALL recommend.collaborativeRecommendations('1')
    YIELD title, commonRatings
    RETURN title, commonRatings

## Similarity Based on Common Genres
### 4) Find movies most similar to Inception based on shared genres
#### CYPHER
    MATCH (inception:Movie {title: 'Inception'})-[:IN_GENRE]->(genre:Genre)<-[:IN_GENRE]-(similar:Movie)
    WHERE inception <> similar
    WITH similar, count(genre) AS sharedGenres
    RETURN similar.title AS recommended_movie, sharedGenres
    ORDER BY sharedGenres DESC;
#### PROC 
    CALL movie.findSimilarMovies('Inception')

## Personalized Recommendations Based on Genres
### 5) Recommend movies similar to those the user has already watched
#### CYPHER
    :params 
    {
    "userName": 'Katie Brown'
    }
    
    MATCH (user:User {name: $userName})-[:RATED]->(watched:Movie)-[:IN_GENRE]->(genre:Genre)<-[:IN_GENRE]-(recommend:Movie)
    WHERE NOT (user)-[:RATED]->(recommend) AND watched <> recommend
    WITH recommend, COUNT(genre) AS sharedGenres
    RETURN recommend.title AS movieTitle, sharedGenres
    ORDER BY sharedGenres DESC
    LIMIT 10;
#### PROC 
    CALL movie.recommendSimilarMovies('Katie Brown')

## Weighted Content Algorithm
### 6) Compute a weighted sum based on the number and types of overlapping traits
#### CYPHER
    Recommendation score calculation for similar films using a weighted sum of the following traits:

    * Genres (weighting: 5x)
    * Actors (weighting: 3x)
    * Directors (weighting: 4x)

    :params 
    {
    "title": 'Inception',  
    }

    MATCH (target:Movie {title: $title})-[:IN_GENRE]->(genre:Genre)<-[:IN_GENRE]-(recommend:Movie)
    OPTIONAL MATCH (target)-[:ACTED_IN]-(actor:Person)<-[:ACTED_IN]-(recommend)
    OPTIONAL MATCH (target)-[:DIRECTED]-(director:Person)<-[:DIRECTED]-(recommend)
    WHERE target <> recommend
    WITH recommend,
    COUNT(DISTINCT genre) * 5 AS genreScore,
    COUNT(DISTINCT actor) * 3 AS actorScore,
    COUNT(DISTINCT director) * 4 AS directorScore
    WITH recommend,
    genreScore + actorScore + directorScore AS totalScore
    RETURN recommend.title AS movieTitle, totalScore
    ORDER BY totalScore DESC
    LIMIT 10;

## Jaccard Index
### 7) What movies are most similar to Inception based on Jaccard similarity of genres?
#### CYPHER
    TODO: Create Cypher query
### 8) Apply this same approach to all "traits" of the movie (genre, actors, directors, etc.):
#### CYPHER 
    TODO: Create Cypher query

## Leveraging Movie Ratings
#### Show all ratings by Misty Williams
    TODO: Create Cypher query
#### Find Misty’s average rating
    TODO: Create Cypher query
#### What are the movies that Misty liked more than average?
    TODO: Create Cypher query

## Collaborative Filtering – The Wisdom of Crowds
### 10) For a particular user, what genres have a higher-than-average rating? Use this to score similar movies
    TODO :
    // 1 compute mean rating
    // 2 find genres with higher than average rating and their number of rated movies
    // 3 find movies in those genres, that have not been watched yet

## Cosine Similarity
### 11) Find the users with the most similar preferences to Cynthia Freeman, according to cosine similarity
      TODO
    // Most similar users using Cosine similarity
### 12) Find the users with the most similar preferences to Cynthia Freeman, according to cosine similarity function
    TODO:
    hint :  gds.similarity.cosine

## Pearson Similarity
### 13) Find users most similar to Cynthia Freeman, according to Pearson similarity
    TODO:
### 14) Find users most similar to Cynthia Freeman, according to the Pearson similarity function
    TODO:

## kNN – K-Nearest Neighbors
### 15) "Who are the 10 users with tastes in movies most similar to mine? What movies have they rated highly that I haven’t seen yet?"
kNN movie recommendation using Pearson similarity

    TODO


## Further Work
### Temporal component
    Preferences change over time, use the rating timestamp to consider how more recent ratings might be used to find more relevant recommendations.
### Keyword extraction
    Enhance the traits available using the plot description. How would you model extracted keywords for movies?
### Image recognition using posters
    There are several libraries and APIs that offer image recognition and tagging.