package fi.haagahelia.projectbackend.repository;

import org.springframework.data.repository.CrudRepository;

import fi.haagahelia.projectbackend.model.Movie;

public interface MovieRepository extends CrudRepository<Movie, Long> {

    Movie findByTitle(String title);
}
