# projet-sda-cloud-2

## Database import
    SHOW PROCEDURES YIELD name WHERE name = 'apoc.cypher.runFile'

    CALL apoc.cypher.runFile('data/all-plain.cypher')

## 1) How many reviews does each 'Matrix' movie have?
### CYPHER
     MATCH (m:Movie)<-[:RATED]-(u:User)
     WHERE m.title CONTAINS 'Matrix'
     WITH m, count(*) AS reviews
     RETURN m.title AS movie, reviews
     ORDER BY reviews DESC LIMIT 5;
### PROC 
    CALL recommend.howManyReview()
    YIELD title, reviews
    RETURN title, reviews

## 2) Find Items similar to the item you’re looking at now
### CYPHER
    :param title => 'Hamlet'

    MATCH (movie:Movie {title: $title})-[:IN_GENRE]->(genre)<-[:IN_GENRE]-(similar:Movie)
    WHERE movie <> similar
    RETURN similar.title AS title, count(genre) AS commonGenres
    ORDER BY commonGenres DESC
    LIMIT 25


### PROC 