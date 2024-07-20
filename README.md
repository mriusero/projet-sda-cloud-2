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
### RESULT
    ╒═════════════════════════╤═══════╕
    │title                    │reviews│
    ╞═════════════════════════╪═══════╡
    │"Matrix, The"            │259    │
    ├─────────────────────────┼───────┤
    │"Matrix Reloaded, The"   │82     │
    ├─────────────────────────┼───────┤
    │"Matrix Revolutions, The"│54     │
    └─────────────────────────┴───────┘

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
#### RESULT
    ╒═══════════════════════════╤════════════╕
    │title                      │commonGenres│
    ╞═══════════════════════════╪════════════╡
    │"Hamlet"                   │34          │
    ├───────────────────────────┼────────────┤
    │"Jane Eyre"                │30          │
    ├───────────────────────────┼────────────┤
    │"Carrie"                   │28          │
    ├───────────────────────────┼────────────┤
    │"Misérables, Les"          │24          │
    ├───────────────────────────┼────────────┤
    │"Star Is Born, A"          │20          │
    ├───────────────────────────┼────────────┤
    │"Trip, The"                │20          │
    ├───────────────────────────┼────────────┤
    │"Phantom of the Opera, The"│20          │
    ├───────────────────────────┼────────────┤
    │"Getaway, The"             │20          │
    ├───────────────────────────┼────────────┤
    │"Twilight"                 │18          │
    ├───────────────────────────┼────────────┤
    │"Bad Boys"                 │18          │
    └───────────────────────────┴────────────┘

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
#### RESULT 
    ╒══════════════════════════════════════════════════════════════════════╤═════════════╕
    │title                                                                 │commonRatings│
    ╞══════════════════════════════════════════════════════════════════════╪═════════════╡
    │"Back to the Future"                                                  │689          │
    ├──────────────────────────────────────────────────────────────────────┼─────────────┤
    │"Star Wars: Episode IV - A New Hope"                                  │688          │
    ├──────────────────────────────────────────────────────────────────────┼─────────────┤
    │"Star Wars: Episode V - The Empire Strikes Back"                      │679          │
    ├──────────────────────────────────────────────────────────────────────┼─────────────┤
    │"Forrest Gump"                                                        │659          │
    ├──────────────────────────────────────────────────────────────────────┼─────────────┤
    │"Pulp Fiction"                                                        │649          │
    ├──────────────────────────────────────────────────────────────────────┼─────────────┤
    │"Raiders of the Lost Ark (Indiana Jones and the Raiders of the Lost Ar│640          │
    │k)"                                                                   │             │
    ├──────────────────────────────────────────────────────────────────────┼─────────────┤
    │"Silence of the Lambs, The"                                           │636          │
    ├──────────────────────────────────────────────────────────────────────┼─────────────┤
    │"Fargo"                                                               │616          │
    ├──────────────────────────────────────────────────────────────────────┼─────────────┤
    │"Jurassic Park"                                                       │616          │
    ├──────────────────────────────────────────────────────────────────────┼─────────────┤
    │"Shawshank Redemption, The"                                           │610          │
    └──────────────────────────────────────────────────────────────────────┴─────────────┘

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
#### RESULT
    ╒══════════════════════════════════════════════════════════════════════╤════════════╕
    │movieTitle                                                            │sharedGenres│
    ╞══════════════════════════════════════════════════════════════════════╪════════════╡
    │"Watchmen"                                                            │6           │
    ├──────────────────────────────────────────────────────────────────────┼────────────┤
    │"Patlabor: The Movie (Kidô keisatsu patorebâ: The Movie)"             │6           │
    ├──────────────────────────────────────────────────────────────────────┼────────────┤
    │"Strange Days"                                                        │6           │
    ├──────────────────────────────────────────────────────────────────────┼────────────┤
    │"RoboCop 3"                                                           │5           │
    ├──────────────────────────────────────────────────────────────────────┼────────────┤
    │"Minority Report"                                                     │5           │
    ├──────────────────────────────────────────────────────────────────────┼────────────┤
    │"Pulse"                                                               │5           │
    ├──────────────────────────────────────────────────────────────────────┼────────────┤
    │"Cellular"                                                            │5           │
    ├──────────────────────────────────────────────────────────────────────┼────────────┤
    │"Man on Fire"                                                         │5           │
    ├──────────────────────────────────────────────────────────────────────┼────────────┤
    │"RoboCop"                                                             │5           │
    ├──────────────────────────────────────────────────────────────────────┼────────────┤
    │"Negotiator, The"                                                     │5           │
    └──────────────────────────────────────────────────────────────────────┴────────────┘

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
#### RESULT
    ╒═══════════════════════════════╤════════════╕
    │movieTitle                     │sharedGenres│
    ╞═══════════════════════════════╪════════════╡
    │"Stunt Man, The"               │100         │
    ├───────────────────────────────┼────────────┤
    │"Rubber"                       │99          │
    ├───────────────────────────────┼────────────┤
    │"Osmosis Jones"                │98          │
    ├───────────────────────────────┼────────────┤
    │"Getaway, The"                 │93          │
    ├───────────────────────────────┼────────────┤
    │"Motorama"                     │92          │
    ├───────────────────────────────┼────────────┤
    │"Hunting Party, The"           │87          │
    ├───────────────────────────────┼────────────┤
    │"Ichi the Killer (Koroshiya 1)"│86          │
    ├───────────────────────────────┼────────────┤
    │"King Solomon's Mines"         │85          │
    ├───────────────────────────────┼────────────┤
    │"Last Boy Scout, The"          │85          │
    ├───────────────────────────────┼────────────┤
    │"Wasabi"                       │85          │
    └───────────────────────────────┴────────────┘

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
#### RESULT
    ╒═════════════════════════════════════════════════════════════════════════╕
    │movieTitle                                                    │totalScore│
    ╞══════════════════════════════════════════════════════════════╪══════════╡
    │"Watchmen"                                                    │30        │
    ├──────────────────────────────────────────────────────────────┼──────────┤
    │"Patlabor: The Movie (Kidô keisatsu patorebâ: The Movie)"     │30        │
    ├──────────────────────────────────────────────────────────────┼──────────┤
    │"Strange Days"                                                │30        │
    ├──────────────────────────────────────────────────────────────┼──────────┤
    │"Sherlock: The Abominable Bride"                              │25        │
    ├──────────────────────────────────────────────────────────────┼──────────┤
    │"X-Files: Fight the Future, The"                              │25        │
    ├──────────────────────────────────────────────────────────────┼──────────┤
    │"Girl Who Played with Fire, The (Flickan som lekte med elden)"│25        │
    ├──────────────────────────────────────────────────────────────┼──────────┤
    │"Whiteout"                                                    │25        │
    ├──────────────────────────────────────────────────────────────┼──────────┤
    │"Kite"                                                        │25        │
    ├──────────────────────────────────────────────────────────────┼──────────┤
    │"Cellular"                                                    │25        │
    ├──────────────────────────────────────────────────────────────┼──────────┤
    │"RoboCop"                                                     │25        │
    └──────────────────────────────────────────────────────────────┴──────────┘

## Jaccard Index
### 7) What movies are most similar to Inception based on Jaccard Index?
#### Genre
    // Trouver les genres associés à Inception
    MATCH (inception:Movie {title: 'Inception'})-[:IN_GENRE]->(genre:Genre)
    WITH inception, COLLECT(genre) AS inceptionGenres
    
    // Trouver les genres pour chaque autre film
    MATCH (similar:Movie)-[:IN_GENRE]->(genre:Genre)
    WHERE inception <> similar
    WITH inception, similar, inceptionGenres, COLLECT(genre) AS similarGenres
    
    // Calculer la taille de l'intersection des genres
    WITH inception, similar,
    REDUCE(intersectionSize = 0, genre IN inceptionGenres |
    CASE WHEN genre IN similarGenres THEN intersectionSize + 1 ELSE intersectionSize END
    ) AS intersectionSize,
    
    // Calculer la taille de l'union des genres
    (SIZE(inceptionGenres) + SIZE(similarGenres) -
    REDUCE(intersectionSize = 0, genre IN inceptionGenres |
    CASE WHEN genre IN similarGenres THEN intersectionSize + 1 ELSE intersectionSize END
    )) AS unionSize
    
    // Calculer l'indice de Jaccard et retourner les résultats
    RETURN similar.title AS similarMovie,
    TOFLOAT(intersectionSize) / TOFLOAT(unionSize) AS jaccardIndex
    ORDER BY jaccardIndex DESC
    LIMIT 10

    ╒══════════════════════════════════════════════════════════════╤══════════════════╕
    │similarMovie                                                  │jaccardIndex      │
    ╞══════════════════════════════════════════════════════════════╪══════════════════╡
    │"Watchmen"                                                    │0.8571428571428571│
    ├──────────────────────────────────────────────────────────────┼──────────────────┤
    │"Strange Days"                                                │0.8571428571428571│
    ├──────────────────────────────────────────────────────────────┼──────────────────┤
    │"Whiteout"                                                    │0.7142857142857143│
    ├──────────────────────────────────────────────────────────────┼──────────────────┤
    │"Kite"                                                        │0.7142857142857143│
    ├──────────────────────────────────────────────────────────────┼──────────────────┤
    │"Girl Who Played with Fire, The (Flickan som lekte med elden)"│0.7142857142857143│
    ├──────────────────────────────────────────────────────────────┼──────────────────┤
    │"Blackhat"                                                    │0.7142857142857143│
    ├──────────────────────────────────────────────────────────────┼──────────────────┤
    │"Double, The"                                                 │0.7142857142857143│
    ├──────────────────────────────────────────────────────────────┼──────────────────┤
    │"Fast Five (Fast and the Furious 5, The)"                     │0.7142857142857143│
    ├──────────────────────────────────────────────────────────────┼──────────────────┤
    │"Sherlock: The Abominable Bride"                              │0.7142857142857143│
    ├──────────────────────────────────────────────────────────────┼──────────────────┤
    │"Source Code"                                                 │0.7142857142857143│
    └──────────────────────────────────────────────────────────────┴──────────────────┘

### 8) Apply this same approach to all "traits" of the movie (genre, actors, directors, etc.):
##### Actor
    // Trouver les acteurs associés à 'Inception'
    MATCH (inception:Movie {title: 'Inception'})-[:ACTED_IN]-(actor:Actor)
    WITH inception, COLLECT(actor) AS inceptionActors
    
    // Trouver les acteurs pour chaque autre film
    MATCH (similar:Movie)-[:ACTED_IN]-(actor:Actor)
    WHERE inception <> similar
    WITH inception, similar, inceptionActors, COLLECT(actor) AS similarActors
    
    // Calculer la taille de l'intersection des acteurs
    WITH inception, similar,
    REDUCE(intersectionSize = 0, actor IN inceptionActors |
    CASE WHEN actor IN similarActors THEN intersectionSize + 1 ELSE intersectionSize END
    ) AS intersectionSize,
    
    // Calculer la taille de l'union des acteurs
    (SIZE(inceptionActors) + SIZE(similarActors) -
    REDUCE(intersectionSize = 0, actor IN inceptionActors |
    CASE WHEN actor IN similarActors THEN intersectionSize + 1 ELSE intersectionSize END
    )) AS unionSize
    
    // Calculer l'indice de Jaccard et retourner les résultats
    RETURN similar.title AS similarMovie,
    TOFLOAT(intersectionSize) / TOFLOAT(unionSize) AS jaccardIndex
    ORDER BY jaccardIndex DESC
    LIMIT 10
    
    ╒═════════════════════════════╤═══════════════════╕
    │similarMovie                 │jaccardIndex       │
    ╞═════════════════════════════╪═══════════════════╡
    │"Dark Knight Rises, The"     │0.3333333333333333 │
    ├─────────────────────────────┼───────────────────┤
    │"The Revenant"               │0.3333333333333333 │
    ├─────────────────────────────┼───────────────────┤
    │"Man in the Iron Mask, The"  │0.14285714285714285│
    ├─────────────────────────────┼───────────────────┤
    │"What's Eating Gilbert Grape"│0.14285714285714285│
    ├─────────────────────────────┼───────────────────┤
    │"Marvin's Room"              │0.14285714285714285│
    ├─────────────────────────────┼───────────────────┤
    │"Titanic"                    │0.14285714285714285│
    ├─────────────────────────────┼───────────────────┤
    │"Basketball Diaries, The"    │0.14285714285714285│
    ├─────────────────────────────┼───────────────────┤
    │"Juror, The"                 │0.14285714285714285│
    ├─────────────────────────────┼───────────────────┤
    │"Quick and the Dead, The"    │0.14285714285714285│
    ├─────────────────────────────┼───────────────────┤
    │"Total Eclipse"              │0.14285714285714285│
    └─────────────────────────────┴───────────────────┘

##### Director
    // Trouver les réalisateurs associés à 'Inception'
    MATCH (inception:Movie {title: 'Inception'})-[:DIRECTED]-(director:Director)
    WITH inception, COLLECT(director) AS inceptionDirectors
    
    // Trouver les réalisateurs pour chaque autre film
    MATCH (similar:Movie)-[:DIRECTED]-(director:Director)
    WHERE inception <> similar
    WITH inception, similar, inceptionDirectors, COLLECT(director) AS similarDirectors
    
    // Calculer la taille de l'intersection des réalisateurs
    WITH inception, similar,
    REDUCE(intersectionSize = 0, director IN inceptionDirectors |
    CASE WHEN director IN similarDirectors THEN intersectionSize + 1 ELSE intersectionSize END
    ) AS intersectionSize,
    
    // Calculer la taille de l'union des réalisateurs
    (SIZE(inceptionDirectors) + SIZE(similarDirectors) -
    REDUCE(intersectionSize = 0, director IN inceptionDirectors |
    CASE WHEN director IN similarDirectors THEN intersectionSize + 1 ELSE intersectionSize END
    )) AS unionSize
    
    // Calculer l'indice de Jaccard et retourner les résultats
    RETURN similar.title AS similarMovie,
    TOFLOAT(intersectionSize) / TOFLOAT(unionSize) AS jaccardIndex
    ORDER BY jaccardIndex DESC
    LIMIT 10

    ╒════════════════════════╤════════════╕
    │similarMovie            │jaccardIndex│
    ╞════════════════════════╪════════════╡
    │"Dark Knight, The"      │1.0         │
    ├────────────────────────┼────────────┤
    │"Batman Begins"         │1.0         │
    ├────────────────────────┼────────────┤
    │"Insomnia"              │1.0         │
    ├────────────────────────┼────────────┤
    │"Interstellar"          │1.0         │
    ├────────────────────────┼────────────┤
    │"Following"             │1.0         │
    ├────────────────────────┼────────────┤
    │"Prestige, The"         │1.0         │
    ├────────────────────────┼────────────┤
    │"Memento"               │1.0         │
    ├────────────────────────┼────────────┤
    │"Dark Knight Rises, The"│1.0         │
    ├────────────────────────┼────────────┤
    │"Paleface, The"         │0.0         │
    ├────────────────────────┼────────────┤
    │"Cameraman, The"        │0.0         │
    └────────────────────────┴────────────┘

## Leveraging Movie Ratings
#### 9) Show all ratings by Misty Williams
    TODO: Create Cypher query


#### 10) Find Misty’s average rating
    TODO: Create Cypher query
#### 11) What are the movies that Misty liked more than average?
    TODO: Create Cypher query

## Collaborative Filtering – The Wisdom of Crowds
### 12) For a particular user, what genres have a higher-than-average rating? Use this to score similar movies
    TODO :
    // 1 compute mean rating
    // 2 find genres with higher than average rating and their number of rated movies
    // 3 find movies in those genres, that have not been watched yet

## Cosine Similarity
### 13) Find the users with the most similar preferences to Cynthia Freeman, according to cosine similarity
      TODO
    // Most similar users using Cosine similarity
### 14) Find the users with the most similar preferences to Cynthia Freeman, according to cosine similarity function
    TODO:
    hint :  gds.similarity.cosine

## Pearson Similarity
### 15) Find users most similar to Cynthia Freeman, according to Pearson similarity
    TODO:
### 16) Find users most similar to Cynthia Freeman, according to the Pearson similarity function
    TODO:

## kNN – K-Nearest Neighbors
### 17) "Who are the 10 users with tastes in movies most similar to mine? What movies have they rated highly that I haven’t seen yet?"
kNN movie recommendation using Pearson similarity

    TODO


## Further Work
### Temporal component
    Preferences change over time, use the rating timestamp to consider how more recent ratings might be used to find more relevant recommendations.
### Keyword extraction
    Enhance the traits available using the plot description. How would you model extracted keywords for movies?
### Image recognition using posters
    There are several libraries and APIs that offer image recognition and tagging.