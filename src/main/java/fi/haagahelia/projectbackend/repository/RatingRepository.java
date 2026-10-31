package fi.haagahelia.projectbackend.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import fi.haagahelia.projectbackend.model.AppUser;
import fi.haagahelia.projectbackend.model.Movie;
import fi.haagahelia.projectbackend.model.Rating;

public interface RatingRepository extends CrudRepository<Rating, Long> {

    Rating findByMovieAndUser(Movie movie, AppUser user);

    List<Rating> findByUser(AppUser user);
}
