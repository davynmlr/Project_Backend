package fi.haagahelia.projectbackend.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import fi.haagahelia.projectbackend.model.Movie;
import fi.haagahelia.projectbackend.repository.MovieRepository;

@RestController
public class MovieRestController {

    @Autowired
    private MovieRepository movieRepository;

    @GetMapping("/movies")
    public List<Movie> movieListRest() {
        return (List<Movie>) movieRepository.findAll();
    }

    @GetMapping("/movies/{id}")
    public Movie findMovieRest(@PathVariable("id") Long movieId) {
        return movieRepository.findById(movieId).get();
    }
}
