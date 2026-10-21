package fi.haagahelia.projectbackend;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class MovieRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void testGetAllMovies() throws Exception {
        this.mockMvc.perform(get("/movies"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Inception")));
    }

    @Test
    @WithMockUser
    public void testGetMovieById() throws Exception {
        this.mockMvc.perform(get("/movies/1"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Inception")));
    }
}
