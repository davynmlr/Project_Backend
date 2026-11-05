# FilmFavourites

A Spring Boot web app for keeping a list of films and rating them. Made for the Backend Programming course at Haaga-Helia.

## Features

- Register, log in and reset your password by email
- Add and edit films (title, director, year, genre)
- Rate films from 1 to 5; the list shows the average rating
- Only admins can delete films
- REST API under `/api` (e.g. `/api/movies`)

## Tech

Java 17, Spring Boot, Spring Security, Spring Data JPA, Thymeleaf, MySQL (H2 for tests).

## Running

```
./mvnw spring-boot:run
```

The app runs on http://localhost:7076. It uses MySQL on softala, so open the SSH tunnel first:

```
ssh -L 3306:localhost:3306 -R 1025:localhost:1025 davynm@softala.haaga-helia.fi
→ take in account that the -R 1025:localhost:1025 is for mailpit, so no need of it if you use the gmail part


```

then in the ssh:

```

java -jar projectbackend-0.0.1-SNAPSHOT.jar

```

Password reset emails go to Mailpit (inbox at http://localhost:8025).

Test users: `user` / `password` and `admin` / `admin`.

## Tests

```

./mvnw test

```

## Deploy

```
`deploy.sh` builds the jar and uploads it to softala.

```

p.s. the frontend is generated with AI
