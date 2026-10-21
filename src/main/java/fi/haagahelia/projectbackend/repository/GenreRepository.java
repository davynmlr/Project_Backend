package fi.haagahelia.projectbackend.repository;

import org.springframework.data.repository.CrudRepository;

import fi.haagahelia.projectbackend.model.Genre;

public interface GenreRepository extends CrudRepository<Genre, Long> {
}
