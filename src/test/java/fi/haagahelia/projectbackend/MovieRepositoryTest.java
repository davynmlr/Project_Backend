package fi.haagahelia.projectbackend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import fi.haagahelia.projectbackend.model.Genre;
import fi.haagahelia.projectbackend.model.Movie;
import fi.haagahelia.projectbackend.repository.GenreRepository;
import fi.haagahelia.projectbackend.repository.MovieRepository;

@DataJpaTest
public class MovieRepositoryTest {

    @Autowired
    private MovieRepository repository;

    @Autowired
    private GenreRepository genreRepository;

    private Movie saveTestMovie() {
        Genre genre = genreRepository.save(new Genre("TestGenre"));
        Movie movie = new Movie("Test Movie", "Test Director", 2024, 3);
        movie.setGenre(genre);
        return repository.save(movie);
    }

    @Test
    public void createNewMovie() {
        Movie movie = saveTestMovie();
        assertThat(movie.getId()).isNotNull();
    }

    @Test
    public void findMovieByTitle() {
        saveTestMovie();
        Movie found = repository.findByTitle("Test Movie");
        assertThat(found.getDirector()).isEqualTo("Test Director");
    }

    @Test
    public void deleteMovie() {
        Movie movie = saveTestMovie();
        repository.delete(movie);
        assertThat(repository.findByTitle("Test Movie")).isNull();
    }
}
