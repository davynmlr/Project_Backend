package fi.haagahelia.projectbackend.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fi.haagahelia.projectbackend.model.AppUser;
import fi.haagahelia.projectbackend.model.Movie;
import fi.haagahelia.projectbackend.model.Rating;
import fi.haagahelia.projectbackend.repository.GenreRepository;
import fi.haagahelia.projectbackend.repository.MovieRepository;
import fi.haagahelia.projectbackend.repository.RatingRepository;
import fi.haagahelia.projectbackend.repository.UserRepository;

@Controller
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/movielist")
    public String listMovies(Model model, Principal principal) {
        List<Movie> movies = (List<Movie>) movieRepository.findAll();
        model.addAttribute("movies", movies);

        // movie id -> the logged-in user's own score
        Map<Long, Integer> myRatings = new HashMap<>();
        AppUser user = userRepository.findByUsername(principal.getName());
        if (user != null) {
            for (Rating rating : ratingRepository.findByUser(user)) {
                myRatings.put(rating.getMovie().getId(), rating.getScore());
            }
        }
        model.addAttribute("myRatings", myRatings);
        return "movielist";
    }

    @RequestMapping("/add")
    public String addMovie(Model model) {
        model.addAttribute("movie", new Movie());
        model.addAttribute("genres", genreRepository.findAll());
        return "addmovie";
    }

    // Used by both the add and edit forms. The rating field only exists on the add form.
    @PostMapping("/save")
    public String saveMovie(Movie movie, @RequestParam(name = "myRating", required = false) Integer myRating,
            Principal principal) {
        Movie saved = movieRepository.save(movie);
        if (myRating != null) {
            saveRating(saved, principal, myRating);
        }
        return "redirect:/movielist";
    }

    @PostMapping("/rate/{id}")
    public String rateMovie(@PathVariable("id") Long movieId, @RequestParam("score") int score,
            Principal principal) {
        movieRepository.findById(movieId).ifPresent(movie -> saveRating(movie, principal, score));
        return "redirect:/movielist";
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
        return "redirect:/movielist";
    }

    // Creates the user's rating for this movie, or updates it if they already rated it
    private void saveRating(Movie movie, Principal principal, int score) {
        if (score < 1 || score > 5) {
            return;
        }
        AppUser user = userRepository.findByUsername(principal.getName());
        if (user == null) {
            return;
        }
        Rating rating = ratingRepository.findByMovieAndUser(movie, user);
        if (rating == null) {
            rating = new Rating(movie, user, score);
        } else {
            rating.setScore(score);
        }
        ratingRepository.save(rating);
    }
}
