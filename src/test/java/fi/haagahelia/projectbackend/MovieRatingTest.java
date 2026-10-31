package fi.haagahelia.projectbackend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import fi.haagahelia.projectbackend.model.Movie;
import fi.haagahelia.projectbackend.repository.MovieRepository;
import fi.haagahelia.projectbackend.repository.RatingRepository;
import fi.haagahelia.projectbackend.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class MovieRatingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "user")
    public void movieListShowsAverageRating() throws Exception {
        // Inception is seeded with ratings 5 and 4
        mockMvc.perform(get("/movielist"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("4.5 / 5 (2)")));
    }

    @Test
    @WithMockUser(username = "user")
    public void ratingTwiceUpdatesInsteadOfAddingSecondRating() throws Exception {
        Movie movie = movieRepository.findByTitle("Spirited Away");

        mockMvc.perform(post("/rate/" + movie.getId()).param("score", "2").with(csrf()))
            .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/rate/" + movie.getId()).param("score", "5").with(csrf()))
            .andExpect(status().is3xxRedirection());

        var user = userRepository.findByUsername("user");
        assertThat(ratingRepository.findByMovieAndUser(movie, user).getScore()).isEqualTo(5);
        long ratingsForMovie = ratingRepository.findByUser(user).stream()
            .filter(r -> r.getMovie().getId().equals(movie.getId()))
            .count();
        assertThat(ratingsForMovie).isEqualTo(1);
    }
}
