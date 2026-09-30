Project for the Backend Programming course (Haaga-Helia).

# Idea 1

# FilmFavourites

A small Spring Boot web application for tracking your favourite films.

## Overview

FilmFavourites lets a logged-in user search for films, add them to a personal favourites list, and rate them. It's a reduced, single-user-list version of a Letterboxd-style app, with the depth going into authentication, external API integration and deployment rather than into a large domain model.

## Features

User registration and login (form-based + OAuth2 social login)
Add / remove films from a personal favourites list
Rate a favourite film (1–5)
Film metadata (title, director, year, poster) auto-filled via the TMDB (The Movie Database) API
REST endpoints alongside the Thymeleaf web UI (optional layer)
