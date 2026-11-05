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

## Mailpit (password reset emails)

Password reset emails are sent to [Mailpit](https://github.com/axllent/mailpit), a fake SMTP server with a web inbox, which runs on softala next to the app. I chose it instead of Gmail or an email service like Brevo because those need a phone number to verify the account (Gmail app passwords require 2-Step Verification) and I didn't want to link my personal phone number to a school project account for security reasons. The reset feature still works fully (token, 30-minute expiry, link, new password); the email just lands in the Mailpit inbox instead of a real one.

Install it on softala (once, no sudo needed):

```
mkdir -p ~/bin && cd ~/bin
curl -sL https://github.com/axllent/mailpit/releases/latest/download/mailpit-linux-amd64.tar.gz | tar xz mailpit
```

Run it in a `screen` so it keeps running after logging out (detach with Ctrl+A, then D):

```
screen -S mailpit ~/bin/mailpit --smtp 127.0.0.1:1025 --listen 127.0.0.1:8025 --database ~/mailpit.db
```

It only listens on `127.0.0.1`, so other softala users can't read the inbox. To see the emails, open a tunnel from your own computer (not from inside softala) and keep it open:

```
ssh -L 8025:localhost:8025 davynm@softala.haaga-helia.fi
```

Then open http://localhost:8025 in the browser.

Test users: `user` / `password` and `admin` / `admin`.

## Tests

```

./mvnw test

```

## Deploy

```
`deploy.sh` builds the jar and uploads it to softala.

```

You can now open http://softala.haaga-helia.fi:7076/login to join the application.

p.s. the frontend is generated with AI
