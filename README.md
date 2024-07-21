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
    MATCH (user:User {name: 'Misty Williams'})-[r:RATED]->(movie:Movie)
    RETURN movie.title AS movieTitle, r.rating AS userRating, r.timestamp AS ratingTimestamp
    ORDER BY movieTitle

    ╒══════════════════════════════════════════════════════════════════════╤══════════╤═══════════════╕
    │movieTitle                                                            │userRating│ratingTimestamp│
    ╞══════════════════════════════════════════════════════════════════════╪══════════╪═══════════════╡
    │"12 Angry Men"                                                        │4.0       │855191702      │
    ├──────────────────────────────────────────────────────────────────────┼──────────┼───────────────┤
    │"20,000 Leagues Under the Sea"                                        │4.0       │855192120      │
    ├──────────────────────────────────────────────────────────────────────┼──────────┼───────────────┤
    │"2001: A Space Odyssey"                                               │4.0       │855191289      │
    ├──────────────────────────────────────────────────────────────────────┼──────────┼───────────────┤
    │"39 Steps, The"                                                       │4.0       │855192061      │
    ├──────────────────────────────────────────────────────────────────────┼──────────┼───────────────┤
    │"A Walk in the Sun"                                                   │4.0       │855194498      │
    ├──────────────────────────────────────────────────────────────────────┼──────────┼───────────────┤
    │"Abyss, The"                                                          │3.0       │855195373      │
    ├──────────────────────────────────────────────────────────────────────┼──────────┼───────────────┤
    │"Ace Ventura: Pet Detective"                                          │3.0       │855194560      │
    ├──────────────────────────────────────────────────────────────────────┼──────────┼───────────────┤
    │"Adventures of Priscilla, Queen of the Desert, The"                   │3.0       │855191289      │
    ├──────────────────────────────────────────────────────────────────────┼──────────┼───────────────┤
    │"Adventures of Robin Hood, The"                                       │4.0       │855192033      │

#### 10) Find Misty’s average rating
    MATCH (user:User {name: 'Misty Williams'})-[r:RATED]->(movie:Movie)
    RETURN user.name AS userName,
    AVG(r.rating) AS averageRating

    ╒════════════════╤══════════════════╕
    │userName        │averageRating     │
    ╞════════════════╪══════════════════╡
    │"Misty Williams"│3.5342789598108744│
    └────────────────┴──────────────────┘

#### 11) What are the movies that Misty liked more than average?
    // 1. Calculer la note moyenne de Misty Williams
    MATCH (user:User {name: 'Misty Williams'})-[r:RATED]->(movie:Movie)
    WITH AVG(r.rating) AS averageRating
    
    // 2. Trouver les films que Misty Williams a notés au-dessus de sa moyenne
    MATCH (user:User {name: 'Misty Williams'})-[r:RATED]->(movie:Movie)
    WHERE r.rating > averageRating
    RETURN movie.title AS movieTitle, r.rating AS userRating
    ORDER BY userRating DESC

    ╒══════════════════════════════════════════════════════════════════════╤══════════╕
    │movieTitle                                                            │userRating│
    ╞══════════════════════════════════════════════════════════════════════╪══════════╡
    │"Raising Arizona"                                                     │5.0       │
    ├──────────────────────────────────────────────────────────────────────┼──────────┤
    │"Jaws"                                                                │5.0       │
    ├──────────────────────────────────────────────────────────────────────┼──────────┤
    │"Breaking the Waves"                                                  │5.0       │
    ├──────────────────────────────────────────────────────────────────────┼──────────┤
    │"Nosferatu (Nosferatu, eine Symphonie des Grauens)"                   │5.0       │
    ├──────────────────────────────────────────────────────────────────────┼──────────┤
    │"Cape Fear"                                                           │5.0       │
    ├──────────────────────────────────────────────────────────────────────┼──────────┤


## Collaborative Filtering – The Wisdom of Crowds
### 12) For a particular user, what genres have a higher-than-average rating? Use this to score similar movies
    // 1 compute mean rating
    // Trouver les notes données par l'utilisateur et associer les genres
    MATCH (user:User {name: 'Misty Williams'})-[r:RATED]->(movie:Movie)-[:IN_GENRE]->(genre:Genre)
    WITH genre, AVG(r.rating) AS averageRatingPerGenre
    RETURN genre.name AS genreName, averageRatingPerGenre
    ORDER BY averageRatingPerGenre DESC

    ╒═════════════╤═════════════════════╕
    │genreName    │averageRatingPerGenre│
    ╞═════════════╪═════════════════════╡
    │"Film-Noir"  │4.333333333333334    │
    ├─────────────┼─────────────────────┤
    │"War"        │4.0                  │
    ├─────────────┼─────────────────────┤
    │"Documentary"│4.0                  │
    ├─────────────┼─────────────────────┤
    │"Children"   │3.923076923076923    │
    ├─────────────┼─────────────────────┤
    │"Animation"  │3.88235294117647     │
    ├─────────────┼─────────────────────┤

    // 2 find genres with higher than average rating and their number of rated movies
    // Calculer la note moyenne globale de l'utilisateur
    MATCH (user:User {name: 'Misty Williams'})-[r:RATED]->(movie:Movie)
    WITH AVG(r.rating) AS averageRating
    
    // Trouver les notes données par l'utilisateur, associer les genres, et compter le nombre de notes par genre
    MATCH (user)-[r:RATED]->(movie:Movie)-[:IN_GENRE]->(genre:Genre)
    WITH genre, AVG(r.rating) AS averageRatingPerGenre, COUNT(r) AS ratingCount, averageRating
    
    // Filtrer les genres avec des notes moyennes supérieures à la moyenne de l'utilisateur
    WHERE averageRatingPerGenre > averageRating
    
    // Retourner les genres avec leur note moyenne et le nombre de notes
    RETURN genre.name AS genreName, averageRatingPerGenre, ratingCount
    ORDER BY averageRatingPerGenre DESC

    ╒════════════════════╤═════════════════════╤═══════════╕
    │genreName           │averageRatingPerGenre│ratingCount│
    ╞════════════════════╪═════════════════════╪═══════════╡
    │"Film-Noir"         │3.9557017543859616   │1140       │
    ├────────────────────┼─────────────────────┼───────────┤
    │"War"               │3.8172139303482524   │5025       │
    ├────────────────────┼─────────────────────┼───────────┤
    │"Documentary"       │3.813299232736575    │1564       │
    ├────────────────────┼─────────────────────┼───────────┤
    │"(no genres listed)"│3.7777777777777772   │18         │
    ├────────────────────┼─────────────────────┼───────────┤
    │"Drama"             │3.6817795852699167   │44752      │
    ├────────────────────┼─────────────────────┼───────────┤
    │"Crime"             │3.679638509775       │16266      │
    ├────────────────────┼─────────────────────┼───────────┤
    │"Mystery"           │3.6795409836065565   │7625       │
    ├────────────────────┼─────────────────────┼───────────┤
    │"Animation"         │3.63606158833063     │6170       │
    ├────────────────────┼─────────────────────┼───────────┤
    │"Musical"           │3.598792884371034    │4722       │
    ├────────────────────┼─────────────────────┼───────────┤
    │"IMAX"              │3.571134347275033    │3156       │
    ├────────────────────┼─────────────────────┼───────────┤
    │"Western"           │3.5664225941422565   │1912       │
    ├────────────────────┼─────────────────────┼───────────┤
    │"Romance"           │3.5561646669425087   │19336      │
    └────────────────────┴─────────────────────┴───────────┘

    // 3 find movies in those genres, that have not been watched yet
    // Calculer la note moyenne globale de l'utilisateur
    MATCH (user:User {name: 'Misty Williams'})-[r:RATED]->(movie:Movie)
    WITH AVG(r.rating) AS averageRating
    
    // Trouver les notes données par l'utilisateur, associer les genres, et compter le nombre de notes par genre
    MATCH (user)-[r:RATED]->(movie:Movie)-[:IN_GENRE]->(genre:Genre)
    WITH genre, AVG(r.rating) AS averageRatingPerGenre, COUNT(r) AS ratingCount, averageRating
    
    // Filtrer les genres avec des notes moyennes supérieures à la moyenne de l'utilisateur
    WHERE averageRatingPerGenre > averageRating
    
    // Trouver les films dans ces genres qui n'ont pas encore été vus par l'utilisateur
    MATCH (genre)<-[:IN_GENRE]-(unwatchedMovie:Movie)
    WHERE NOT EXISTS {
    MATCH (user)-[r:RATED]->(unwatchedMovie)
    }
    RETURN genre.name AS genreName, unwatchedMovie.title AS movieTitle, averageRatingPerGenre, ratingCount
    ORDER BY averageRatingPerGenre DESC

    ╒════════════════════╤════════════════════════════════════════════════════════════════╤═════════════════════╤═══════════╕
    │genreName           │movieTitle                                                      │averageRatingPerGenre│ratingCount│
    ╞════════════════════╪════════════════════════════════════════════════════════════════╪═════════════════════╪═══════════╡
    │"Film-Noir"         │"Nightfall"                                                     │3.9557017543859616   │1140       │
    ├────────────────────┼────────────────────────────────────────────────────────────────┼─────────────────────┼───────────┤
    │"Film-Noir"         │"Kansas City Confidential"                                      │3.9557017543859616   │1140       │
    ├────────────────────┼────────────────────────────────────────────────────────────────┼─────────────────────┼───────────┤
    ...


## Cosine Similarity
### 13) Find the users with the most similar preferences to Cynthia Freeman, according to cosine similarity
    // Most similar users using Cosine similarity
    // 1. Récupérer les notes de Cynthia Freeman
    WITH 'Cynthia Freeman' AS targetUser
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(movie:Movie)
    WITH targetUser, COLLECT(movie) AS targetMovies, COLLECT(r1.rating) AS targetRatings
    
    // 2. Récupérer les notes des autres utilisateurs
    MATCH (otherUser:User)-[r2:RATED]->(movie:Movie)
    WHERE otherUser.name <> targetUser
    WITH targetUser, targetMovies, targetRatings, otherUser, COLLECT(movie) AS otherMovies, COLLECT(r2.rating) AS otherRatings
    
    // 3. Trouver les films communs et les notes correspondantes
    WITH targetUser, otherUser,
    [m IN targetMovies WHERE m IN otherMovies] AS commonMovies
    
    // Associer les notes de Cynthia et des autres utilisateurs pour les films communs
    WITH targetUser, otherUser, commonMovies
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    COLLECT(r1.rating) AS targetCommonRatings
    
    MATCH (otherUser)-[r2:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    targetCommonRatings, COLLECT(r2.rating) AS otherCommonRatings
    
    // 4. Calculer le produit scalaire et les normes
    WITH targetUser, otherUser,
    REDUCE(sum = 0, i IN RANGE(0, SIZE(commonMovies) - 1) |
    sum + (targetCommonRatings[i] * otherCommonRatings[i])) AS dotProduct,
    REDUCE(sum = 0, i IN RANGE(0, SIZE(targetCommonRatings) - 1) |
    sum + (targetCommonRatings[i] * targetCommonRatings[i])) AS normTarget,
    REDUCE(sum = 0, i IN RANGE(0, SIZE(otherCommonRatings) - 1) |
    sum + (otherCommonRatings[i] * otherCommonRatings[i])) AS normOther
    
    // 5. Calculer la similarité cosinus
    WITH targetUser, otherUser,
    CASE WHEN normTarget = 0 OR normOther = 0 THEN 0 ELSE dotProduct / (SQRT(normTarget) * SQRT(normOther)) END AS cosineSimilarity
    
    // 6. Retourner les utilisateurs les plus similaires
    RETURN otherUser.name AS similarUser, cosineSimilarity
    ORDER BY cosineSimilarity DESC
    LIMIT 10
    
    ╒═════════════════╤══════════════════╕
    │similarUser      │cosineSimilarity  │
    ╞═════════════════╪══════════════════╡
    │"James Whitehead"│1.0000000000000002│
    ├─────────────────┼──────────────────┤
    │"Angela Watkins" │1.0               │
    ├─────────────────┼──────────────────┤
    │"Susan Burnett"  │1.0               │
    ├─────────────────┼──────────────────┤
    │"Julia Shaffer"  │1.0               │
    ├─────────────────┼──────────────────┤
    │"Heather Mccoy"  │1.0               │
    ├─────────────────┼──────────────────┤
    │"Jessica Wilson" │1.0               │
    ├─────────────────┼──────────────────┤
    │"Tracey Irwin"   │1.0               │
    ├─────────────────┼──────────────────┤
    │"Kim Brooks"     │1.0               │
    ├─────────────────┼──────────────────┤
    │"Wayne Smith"    │1.0               │
    ├─────────────────┼──────────────────┤
    │"Randy Blake"    │1.0               │
    └─────────────────┴──────────────────┘


### 14) Find the users with the most similar preferences to Cynthia Freeman, according to cosine similarity function
    // 1. Définir l'utilisateur cible et collecter ses films et notes
    WITH 'Cynthia Freeman' AS targetUser
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(movie:Movie)
    WITH targetUser, COLLECT(movie) AS targetMovies, COLLECT(r1.rating) AS targetRatings
    
    // 2. Récupérer les notes des autres utilisateurs
    MATCH (otherUser:User)-[r2:RATED]->(movie:Movie)
    WHERE otherUser.name <> targetUser
    WITH targetUser, otherUser, targetMovies, COLLECT(movie) AS otherMovies, COLLECT(r2.rating) AS otherRatings
    
    // 3. Trouver les films communs
    WITH targetUser, otherUser, targetMovies, otherMovies,
    [m IN targetMovies WHERE m IN otherMovies] AS commonMovies
    
    // 4. Associer les notes de Cynthia et des autres utilisateurs pour les films communs
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    COLLECT(r1.rating) AS targetCommonRatings
    
    MATCH (otherUser)-[r2:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    targetCommonRatings, COLLECT(r2.rating) AS otherCommonRatings
    
    // 5. Calculer la similarité cosinus
    WITH otherUser,
    gds.similarity.cosine(targetCommonRatings, otherCommonRatings) AS cosineSimilarity
    WHERE cosineSimilarity IS NOT NULL
    
    // 6. Retourner les utilisateurs avec leur similarité
    RETURN otherUser, cosineSimilarity
    ORDER BY cosineSimilarity DESC
    LIMIT 10

    ╒═══════════════════════════════════════════════╤════════════════╕
    │otherUser                                      │cosineSimilarity│
    ╞═══════════════════════════════════════════════╪════════════════╡
    │(:User {name: "Linda Whitaker",userId: "54"})  │1.0             │
    ├───────────────────────────────────────────────┼────────────────┤
    │(:User {name: "Kevin Hill",userId: "37"})      │1.0             │
    ├───────────────────────────────────────────────┼────────────────┤
    │(:User {name: "Patricia Johnson",userId: "53"})│1.0             │
    ├───────────────────────────────────────────────┼────────────────┤
    │(:User {name: "Jessica Wilson",userId: "6"})   │1.0             │
    ├───────────────────────────────────────────────┼────────────────┤
    │(:User {name: "Wayne Smith",userId: "16"})     │1.0             │
    ├───────────────────────────────────────────────┼────────────────┤
    │(:User {name: "Andrew Willis",userId: "31"})   │1.0             │
    ├───────────────────────────────────────────────┼────────────────┤
    │(:User {name: "Tracey Irwin",userId: "9"})     │1.0             │
    ├───────────────────────────────────────────────┼────────────────┤
    │(:User {name: "Michael Stone",userId: "28"})   │1.0             │
    ├───────────────────────────────────────────────┼────────────────┤
    │(:User {name: "Kim Brooks",userId: "12"})      │1.0             │
    ├───────────────────────────────────────────────┼────────────────┤
    │(:User {name: "John Wiggins",userId: "62"})    │1.0             │
    └───────────────────────────────────────────────┴────────────────┘

## Pearson Similarity
### 15) Find users most similar to Cynthia Freeman, according to Pearson similarity
    // 1. Récupérer les notes de Cynthia Freeman
    WITH 'Cynthia Freeman' AS targetUser
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(movie:Movie)
    WITH targetUser, COLLECT(movie) AS targetMovies, COLLECT(r1.rating) AS targetRatings
    
    // 2. Récupérer les notes des autres utilisateurs
    MATCH (otherUser:User)-[r2:RATED]->(movie:Movie)
    WHERE otherUser.name <> targetUser
    WITH targetUser, targetMovies, targetRatings, otherUser, COLLECT(movie) AS otherMovies, COLLECT(r2.rating) AS otherRatings
    
    // 3. Trouver les films communs et les notes correspondantes
    WITH targetUser, otherUser, targetMovies, targetRatings, otherMovies, otherRatings,
    [m IN targetMovies WHERE m IN otherMovies] AS commonMovies
    
    // Associer les notes de Cynthia et des autres utilisateurs pour les films communs
    WITH targetUser, otherUser, commonMovies
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    COLLECT(r1.rating) AS targetCommonRatings
    
    MATCH (otherUser)-[r2:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    targetCommonRatings, COLLECT(r2.rating) AS otherCommonRatings
    
    // 4. Calculer les moyennes des notes pour Cynthia et les autres utilisateurs
    WITH targetUser, otherUser, commonMovies, targetCommonRatings, otherCommonRatings,
    REDUCE(sum = 0, i IN RANGE(0, SIZE(targetCommonRatings) - 1) | sum + targetCommonRatings[i]) / SIZE(targetCommonRatings) AS meanTarget,
    REDUCE(sum = 0, i IN RANGE(0, SIZE(otherCommonRatings) - 1) | sum + otherCommonRatings[i]) / SIZE(otherCommonRatings) AS meanOther
    
    // 5. Calculer les produits croisés des écarts par rapport aux moyennes et les écarts quadratiques moyens
    WITH targetUser, otherUser, meanTarget, meanOther,
    REDUCE(sum = 0, i IN RANGE(0, SIZE(targetCommonRatings) - 1) |
    sum + ((targetCommonRatings[i] - meanTarget) * (otherCommonRatings[i] - meanOther))) AS covariance,
    REDUCE(sum = 0, i IN RANGE(0, SIZE(targetCommonRatings) - 1) |
    sum + ((targetCommonRatings[i] - meanTarget) * (targetCommonRatings[i] - meanTarget))) AS varianceTarget,
    REDUCE(sum = 0, i IN RANGE(0, SIZE(otherCommonRatings) - 1) |
    sum + ((otherCommonRatings[i] - meanOther) * (otherCommonRatings[i] - meanOther))) AS varianceOther
    
    // 6. Calculer la similarité de Pearson
    WITH targetUser, otherUser,
    CASE WHEN SQRT(varianceTarget) = 0 OR SQRT(varianceOther) = 0 THEN 0 ELSE covariance / (SQRT(varianceTarget) * SQRT(varianceOther)) END AS pearsonSimilarity
    
    // 7. Retourner les utilisateurs les plus similaires
    RETURN otherUser.name AS similarUser, pearsonSimilarity
    ORDER BY pearsonSimilarity DESC
    LIMIT 10

    ╒══════════════════╤══════════════════╕
    │similarUser       │pearsonSimilarity │
    ╞══════════════════╪══════════════════╡
    │"Cynthia Owens"   │1.0000000000000002│
    ├──────────────────┼──────────────────┤
    │"James Whitehead" │1.0000000000000002│
    ├──────────────────┼──────────────────┤
    │"Hannah Armstrong"│1.0000000000000002│
    ├──────────────────┼──────────────────┤
    │"Dawn Wood"       │1.0000000000000002│
    ├──────────────────┼──────────────────┤
    │"Steven Jones"    │1.0               │
    ├──────────────────┼──────────────────┤
    │"Amy Shelton"     │1.0               │
    ├──────────────────┼──────────────────┤
    │"Kristin Johnson" │1.0               │
    ├──────────────────┼──────────────────┤
    │"Linda Brown"     │1.0               │
    ├──────────────────┼──────────────────┤
    │"Craig Bowman"    │1.0               │
    ├──────────────────┼──────────────────┤
    │"Leslie Brady"    │1.0               │
    └──────────────────┴──────────────────┘
### 16) Find users most similar to Cynthia Freeman, according to the Pearson similarity function
    // 1. Définir l'utilisateur cible et collecter ses films et notes
    WITH 'Cynthia Freeman' AS targetUser
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(movie:Movie)
    WITH targetUser, COLLECT(movie) AS targetMovies, COLLECT(r1.rating) AS targetRatings
    
    // 2. Récupérer les notes des autres utilisateurs
    MATCH (otherUser:User)-[r2:RATED]->(movie:Movie)
    WHERE otherUser.name <> targetUser
    WITH targetUser, otherUser, targetMovies, COLLECT(movie) AS otherMovies, COLLECT(r2.rating) AS otherRatings
    
    // 3. Trouver les films communs
    WITH targetUser, otherUser, targetMovies, otherMovies,
    [m IN targetMovies WHERE m IN otherMovies] AS commonMovies
    
    // 4. Associer les notes de Cynthia et des autres utilisateurs pour les films communs
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    COLLECT(r1.rating) AS targetCommonRatings
    
    MATCH (otherUser)-[r2:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    targetCommonRatings, COLLECT(r2.rating) AS otherCommonRatings
    
    // 5. Calculer la similarité de Pearson
    WITH otherUser,
    gds.similarity.pearson(targetCommonRatings, otherCommonRatings) AS pearsonSimilarity
    WHERE pearsonSimilarity IS NOT NULL
    
    // 6. Retourner les utilisateurs avec leur similarité
    RETURN otherUser, pearsonSimilarity
    ORDER BY pearsonSimilarity DESC
    LIMIT 10

    ╒═══════════════════════════════════════════════╤═════════════════╕
    │otherUser                                      │pearsonSimilarity│
    ╞═══════════════════════════════════════════════╪═════════════════╡
    │(:User {name: "Jake Mathews",userId: "166"})   │1.0              │
    ├───────────────────────────────────────────────┼─────────────────┤
    │(:User {name: "Alison Cooper",userId: "209"})  │1.0              │
    ├───────────────────────────────────────────────┼─────────────────┤
    │(:User {name: "Tammy Martinez",userId: "98"})  │1.0              │
    ├───────────────────────────────────────────────┼─────────────────┤
    │(:User {name: "Crystal Strong",userId: "142"}) │1.0              │
    ├───────────────────────────────────────────────┼─────────────────┤
    │(:User {name: "Steven Jones",userId: "204"})   │1.0              │
    ├───────────────────────────────────────────────┼─────────────────┤
    │(:User {name: "Cynthia Owens",userId: "33"})   │1.0              │
    ├───────────────────────────────────────────────┼─────────────────┤
    │(:User {name: "Hannah Armstrong",userId: "79"})│1.0              │
    ├───────────────────────────────────────────────┼─────────────────┤
    │(:User {name: "Margaret Allen",userId: "10"})  │1.0              │
    ├───────────────────────────────────────────────┼─────────────────┤
    │(:User {name: "Jeffrey Myers",userId: "141"})  │1.0              │
    ├───────────────────────────────────────────────┼─────────────────┤
    │(:User {name: "Amy Shelton",userId: "218"})    │1.0              │
    └───────────────────────────────────────────────┴─────────────────┘

## kNN – K-Nearest Neighbors
### 17) kNN movie recommendation using Pearson similarity
    // 1. Définir l'utilisateur cible
    WITH 'Cynthia Freeman' AS targetUser
    
    // 2. Obtenir les 10 utilisateurs les plus similaires
    CALL {
    WITH targetUser
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(movie:Movie)
    WITH targetUser, COLLECT(movie) AS targetMovies, COLLECT(r1.rating) AS targetRatings

    // Trouver les autres utilisateurs et leurs évaluations
    MATCH (otherUser:User)-[r2:RATED]->(movie:Movie)
    WHERE otherUser.name <> targetUser
    WITH targetUser, otherUser, targetMovies, COLLECT(movie) AS otherMovies, COLLECT(r2.rating) AS otherRatings

    // Identifier les films communs
    WITH targetUser, otherUser, targetMovies, otherMovies,
    [m IN targetMovies WHERE m IN otherMovies] AS commonMovies

    // Extraire les évaluations communes pour l'utilisateur cible
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    COLLECT(r1.rating) AS targetCommonRatings

    // Extraire les évaluations communes pour les autres utilisateurs
    MATCH (otherUser)-[r2:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH otherUser, targetCommonRatings, COLLECT(r2.rating) AS otherCommonRatings

    // Calculer la similarité de Pearson
    WITH otherUser,
    gds.similarity.pearson(targetCommonRatings, otherCommonRatings) AS pearsonSimilarity
    WHERE pearsonSimilarity IS NOT NULL

    RETURN otherUser AS similarUser, pearsonSimilarity
    ORDER BY pearsonSimilarity DESC
    LIMIT 10
    }
    
    // 3. Recommander des films basés sur les utilisateurs similaires
    MATCH (similarUser)-[r:RATED]->(movie:Movie)
    WHERE NOT EXISTS {
    MATCH (targetUserNode:User {name: targetUser})-[r2:RATED]->(movie)
    }
    RETURN movie.title AS recommendedMovie, COUNT(similarUser) AS recommendationScore
    ORDER BY recommendationScore DESC
    LIMIT 10

    ╒══════════════════════════════════════════════════════════════════════╤═══════════════════╕
    │recommendedMovie                                                      │recommendationScore│
    ╞══════════════════════════════════════════════════════════════════════╪═══════════════════╡
    │"Forrest Gump"                                                        │5                  │
    ├──────────────────────────────────────────────────────────────────────┼───────────────────┤
    │"Star Wars: Episode V - The Empire Strikes Back"                      │5                  │
    ├──────────────────────────────────────────────────────────────────────┼───────────────────┤
    │"Raiders of the Lost Ark (Indiana Jones and the Raiders of the Lost Ar│4                  │
    │k)"                                                                   │                   │
    ├──────────────────────────────────────────────────────────────────────┼───────────────────┤
    │"Princess Bride, The"                                                 │4                  │
    ├──────────────────────────────────────────────────────────────────────┼───────────────────┤
    │"Toy Story 2"                                                         │3                  │
    ├──────────────────────────────────────────────────────────────────────┼───────────────────┤
    │"Indiana Jones and the Last Crusade"                                  │3                  │
    ├──────────────────────────────────────────────────────────────────────┼───────────────────┤
    │"Titanic"                                                             │3                  │
    ├──────────────────────────────────────────────────────────────────────┼───────────────────┤
    │"Jurassic Park"                                                       │3                  │
    ├──────────────────────────────────────────────────────────────────────┼───────────────────┤
    │"Star Wars: Episode IV - A New Hope"                                  │3                  │
    ├──────────────────────────────────────────────────────────────────────┼───────────────────┤
    │"Ferris Bueller's Day Off"                                            │3                  │
    └──────────────────────────────────────────────────────────────────────┴───────────────────┘

## Further Work
### Temporal component
#### Cypher query
    Preferences change over time, use the rating timestamp to consider how more recent 
    ratings might be used to find more relevant recommendations.

    // 1. Définir l'utilisateur cible
    WITH 'Cynthia Freeman' AS targetUser
    
    // 2. Obtenir les 10 utilisateurs les plus similaires
    CALL {
    WITH targetUser
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(movie:Movie)
    WITH targetUser, COLLECT(movie) AS targetMovies, COLLECT({rating: r1.rating, timestamp: r1.timestamp}) AS targetRatings

    // Trouver les autres utilisateurs et leurs évaluations
    MATCH (otherUser:User)-[r2:RATED]->(movie:Movie)
    WHERE otherUser.name <> targetUser
    WITH targetUser, otherUser, targetMovies, COLLECT(movie) AS otherMovies, COLLECT({rating: r2.rating, timestamp: r2.timestamp}) AS otherRatings

    // Identifier les films communs
    WITH targetUser, otherUser, targetMovies, otherMovies,
    [m IN targetMovies WHERE m IN otherMovies] AS commonMovies

    // Extraire les évaluations communes pour l'utilisateur cible
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    COLLECT({rating: r1.rating, timestamp: r1.timestamp}) AS targetCommonRatings

    // Extraire les évaluations communes pour les autres utilisateurs
    MATCH (otherUser)-[r2:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH otherUser, targetCommonRatings,
    COLLECT({rating: r2.rating, timestamp: r2.timestamp}) AS otherCommonRatings

    // Calculer la similarité de Pearson avec pondération
    WITH otherUser,
    gds.similarity.pearson(
        [x IN targetCommonRatings | x.rating * EXP(-0.01 * (timestamp() - x.timestamp))],
        [x IN otherCommonRatings | x.rating * EXP(-0.01 * (timestamp() - x.timestamp))]
    ) AS pearsonSimilarity
    WHERE pearsonSimilarity IS NOT NULL

    RETURN otherUser AS similarUser, pearsonSimilarity
    ORDER BY pearsonSimilarity DESC
    LIMIT 10
    }
    
    // 3. Recommander des films basés sur les utilisateurs similaires
    MATCH (similarUser)-[r:RATED]->(movie:Movie)
    WHERE NOT EXISTS {
    MATCH (targetUserNode:User {name: targetUser})-[r2:RATED]->(movie)
    }
    RETURN movie.title AS recommendedMovie, COUNT(similarUser) AS recommendationScore
    ORDER BY recommendationScore DESC
    LIMIT 10
#### Cypher query
    CALL movie.getSimilarUserRecommendations('Cynthia Freeman')
    YIELD recommendedMovie, recommendationScore
    RETURN recommendedMovie, recommendationScore
#### RESULT
    ╒════════════════════════════════════════════╤═══════════════════╕
    │recommendedMovie                            │recommendationScore│
    ╞════════════════════════════════════════════╪═══════════════════╡
    │"Forrest Gump"                              │6                  │
    ├────────────────────────────────────────────┼───────────────────┤
    │"Princess Bride, The"                       │6                  │
    ├────────────────────────────────────────────┼───────────────────┤
    │"Star Wars: Episode VI - Return of the Jedi"│5                  │
    ├────────────────────────────────────────────┼───────────────────┤
    │"Sixth Sense, The"                          │4                  │
    ├────────────────────────────────────────────┼───────────────────┤
    │"Pulp Fiction"                              │4                  │
    ├────────────────────────────────────────────┼───────────────────┤
    │"Schindler's List"                          │4                  │
    ├────────────────────────────────────────────┼───────────────────┤
    │"Silence of the Lambs, The"                 │4                  │
    ├────────────────────────────────────────────┼───────────────────┤
    │"Batman"                                    │4                  │
    ├────────────────────────────────────────────┼───────────────────┤
    │"Speed"                                     │4                  │
    ├────────────────────────────────────────────┼───────────────────┤
    │"Indiana Jones and the Last Crusade"        │4                  │
    └────────────────────────────────────────────┴───────────────────┘

# In Progress

### Keyword extraction
    Enhance the traits available using the plot description. How would you model extracted keywords for movies?

    // 1. Définir l'utilisateur cible
    WITH 'Cynthia Freeman' AS targetUser
    
    // 2. Obtenir les 10 utilisateurs les plus similaires
    CALL {
    WITH targetUser
    MATCH (targetUserNode:User {name: targetUser})-[r1:RATED]->(movie:Movie)
    WITH targetUser, COLLECT(movie) AS targetMovies, COLLECT({rating: r1.rating, timestamp: r1.timestamp}) AS targetRatings

    // Trouver les autres utilisateurs et leurs évaluations
    MATCH (otherUser:User)-[r2:RATED]->(movie:Movie)
    WHERE otherUser.name <> targetUser
    WITH targetUser, otherUser, targetMovies, COLLECT(movie) AS otherMovies, COLLECT({rating: r2.rating, timestamp: r2.timestamp}) AS otherRatings

    // Identifier les films communs
    WITH targetUser, otherUser, targetMovies, otherMovies,
    [m IN targetMovies WHERE m IN otherMovies] AS commonMovies

    // Extraire les évaluations communes pour l'utilisateur cible
    MATCH (targetUserNode)-[r1:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH targetUser, otherUser, commonMovies,
    COLLECT({rating: r1.rating, timestamp: r1.timestamp}) AS targetCommonRatings

    // Extraire les évaluations communes pour les autres utilisateurs
    MATCH (otherUser)-[r2:RATED]->(m:Movie)
    WHERE m IN commonMovies
    WITH otherUser, targetCommonRatings,
    COLLECT({rating: r2.rating, timestamp: r2.timestamp}) AS otherCommonRatings

    // Filtrer les utilisateurs avec des vecteurs valides
    WITH otherUser, targetCommonRatings, otherCommonRatings
    WHERE SIZE(targetCommonRatings) > 0 AND SIZE(otherCommonRatings) > 0
    AND SIZE(targetCommonRatings) = SIZE(otherCommonRatings)

    // Calculer la similarité de Pearson avec pondération
    WITH otherUser,
    gds.similarity.pearson(
        [x IN targetCommonRatings | x.rating * EXP(-0.01 * (timestamp() - x.timestamp))],
        [x IN otherCommonRatings | x.rating * EXP(-0.01 * (timestamp() - x.timestamp))]
    ) AS pearsonSimilarity
    WHERE pearsonSimilarity IS NOT NULL

    RETURN otherUser AS similarUser, pearsonSimilarity
    ORDER BY pearsonSimilarity DESC
    LIMIT 10
    }
    
    // 3. Recommander des films basés sur les utilisateurs similaires
    MATCH (similarUser)-[r:RATED]->(movie:Movie)
    WHERE NOT EXISTS {
    MATCH (targetUserNode:User {name: targetUser})-[r2:RATED]->(movie)
    }
    WITH movie, COLLECT(similarUser) AS similarUsers
    
    // Obtenez les mots-clés pour les films recommandés
    MATCH (movie)-[:HAS_KEYWORDS]->(keyword:Keyword)
    WITH movie, similarUsers, COLLECT(keyword.word) AS movieKeywords
    
    // Obtenez les mots-clés pour les films évalués par l'utilisateur cible
    MATCH (targetUserNode)-[r1:RATED]->(targetMovie:Movie)
    MATCH (targetMovie)-[:HAS_KEYWORDS]->(keyword:Keyword)
    WITH movie, similarUsers, movieKeywords,
    COLLECT(keyword.word) AS targetMovieKeywords
    
    // Calculer la similarité basée sur les mots-clés (ex: Jaccard)
    WITH movie, similarUsers, movieKeywords, targetMovieKeywords,
    SIZE([k IN movieKeywords WHERE k IN targetMovieKeywords]) * 1.0 / SIZE(movieKeywords + targetMovieKeywords) AS keywordSimilarity
    
    // Calculer le score de recommandation basé sur le nombre d'utilisateurs similaires
    WITH movie, SIZE(similarUsers) AS userRecommendationScore, keywordSimilarity
    
    // Calculer le score total combiné
    WITH movie,
    userRecommendationScore,
    keywordSimilarity,
    (userRecommendationScore * 0.7 + keywordSimilarity * 0.3) AS combinedScore
    
    RETURN movie.title AS recommendedMovie, combinedScore
    ORDER BY combinedScore DESC
    LIMIT 10

    CALL movie.getSimilarEnhancedRecommendations('Cynthia Freeman')
    YIELD recommendedMovie, recommendationScore
    RETURN recommendedMovie, recommendationScore



### Extract keyword (process with nltk via Python)
    CALL movie.ExtractKeyword(
    "James Bond teams up with the lone survivor of a destroyed Russian research center to stop the hijacking of a nuclear space weapon by a fellow agent formerly believed to be dead."
    ) YIELD message
    RETURN message

    ["james", "bond", "teams", "lone", "survivor", "destroyed", "russian", "research", "center", "stop"]



### Image recognition using posters
    There are several libraries and APIs that offer image recognition and tagging.





    
