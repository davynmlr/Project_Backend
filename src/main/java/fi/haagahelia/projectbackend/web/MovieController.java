package fi.haagahelia.projectbackend.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fi.haagahelia.projectbackend.model.Movie;
import fi.haagahelia.projectbackend.repository.GenreRepository;
import fi.haagahelia.projectbackend.repository.MovieRepository;

@Controller
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @RequestMapping("/movielist")
    public String listMovies(Model model) {
        List<Movie> movies = (List<Movie>) movieRepository.findAll();
        model.addAttribute("movies", movies);
        return "movielist";
    }

    @RequestMapping("/add")
    public String addMovie(Model model) {
        model.addAttribute("movie", new Movie());
        model.addAttribute("genres", genreRepository.findAll());
        return "addmovie";
    }

    @PostMapping("/save")
    public String saveMovie(Movie movie) {
        movieRepository.save(movie);
        return "redirect:movielist";
    }

    @RequestMapping("/edit/{id}")
    public String editMovie(@PathVariable("id") Long movieId, Model model) {
        model.addAttribute("movie", movieRepository.findById(movieId).get());
        model.addAttribute("genres", genreRepository.findAll());
        return "editmovie";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/delete/{id}")
    public String deleteMovie(@PathVariable("id") Long movieId) {
        movieRepository.deleteById(movieId);
        return "redirect:../movielist";
    }
}
