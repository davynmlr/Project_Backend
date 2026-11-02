package fi.haagahelia.projectbackend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import fi.haagahelia.projectbackend.model.AppUser;
import fi.haagahelia.projectbackend.model.Genre;
import fi.haagahelia.projectbackend.model.Movie;
import fi.haagahelia.projectbackend.model.Rating;
import fi.haagahelia.projectbackend.repository.GenreRepository;
import fi.haagahelia.projectbackend.repository.MovieRepository;
import fi.haagahelia.projectbackend.repository.RatingRepository;
import fi.haagahelia.projectbackend.repository.UserRepository;

@SpringBootApplication
public class ProjectbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectbackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(MovieRepository movieRepository, GenreRepository genreRepository, UserRepository userRepository,
			RatingRepository ratingRepository) {
		return (args) -> {
			// The database keeps its data between restarts, so only seed an empty one.
			// Users are checked (not movies) because an admin can delete every movie,
			// and re-creating "user"/"admin" would break their unique username.
			if (userRepository.count() > 0) {
				return;
			}

			// GENRES
			Genre sciFi = genreRepository.save(new Genre("Science Fiction"));
			Genre drama = genreRepository.save(new Genre("Drama"));
			Genre crime = genreRepository.save(new Genre("Crime"));
			Genre animation = genreRepository.save(new Genre("Animation"));

			// MOVIES
			Movie inception = new Movie("Inception", "Christopher Nolan", 2010);
			inception.setGenre(sciFi);
			movieRepository.save(inception);

			Movie godfather = new Movie("The Godfather", "Francis Ford Coppola", 1972);
			godfather.setGenre(crime);
			movieRepository.save(godfather);

			Movie amelie = new Movie("Amélie", "Jean-Pierre Jeunet", 2001);
			amelie.setGenre(drama);
			movieRepository.save(amelie);

			Movie spiritedAway = new Movie("Spirited Away", "Hayao Miyazaki", 2001);
			spiritedAway.setGenre(animation);
			movieRepository.save(spiritedAway);

			Movie bladeRunner = new Movie("Blade Runner", "Ridley Scott", 1982);
			bladeRunner.setGenre(sciFi);
			movieRepository.save(bladeRunner);

			// USERS
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			AppUser user = userRepository.save(new AppUser("user", encoder.encode("password"), "user@example.com", "USER"));
			AppUser admin = userRepository.save(new AppUser("admin", encoder.encode("admin"), "admin@example.com", "ADMIN"));

			// RATINGS (each user rates movies separately; the list shows the average)
			ratingRepository.save(new Rating(inception, user, 5));
			ratingRepository.save(new Rating(inception, admin, 4));
			ratingRepository.save(new Rating(godfather, admin, 5));
			ratingRepository.save(new Rating(amelie, user, 4));
			ratingRepository.save(new Rating(bladeRunner, user, 3));
			ratingRepository.save(new Rating(bladeRunner, admin, 4));
		};
	}

}
