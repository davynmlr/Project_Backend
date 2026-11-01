package fi.haagahelia.projectbackend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import fi.haagahelia.projectbackend.model.AppUser;
import fi.haagahelia.projectbackend.repository.UserRepository;

// Each test runs in a transaction that is rolled back, so new users don't leak into other tests
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Visitors must be able to reach the sign-up page without being logged in
    @Test
    public void registerPageIsOpenWithoutLogin() throws Exception {
        mockMvc.perform(get("/register"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Create an account")));
    }

    // A valid sign-up saves the user and sends them to the login page.
    // The username is trimmed, the email is lowercased, the role is USER,
    // and the password is stored as a BCrypt hash, never as plain text.
    @Test
    public void validRegistrationCreatesUserWithHashedPassword() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                .param("username", "  newuser  ")
                .param("email", "New.User@Example.com")
                .param("password", "secret123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?registered"));

        AppUser saved = userRepository.findByUsername("newuser");
        assertThat(saved).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("new.user@example.com");
        assertThat(saved.getRole()).isEqualTo("USER");
        assertThat(saved.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", saved.getPasswordHash())).isTrue();
    }

    // "user" is created by the demo data, so registering it again must fail
    // and show the form again (200) instead of redirecting
    @Test
    public void takenUsernameIsRejected() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                .param("username", "user")
                .param("email", "someone@example.com")
                .param("password", "secret123"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Username is already taken")));
    }

    // user@example.com already exists; the check must ignore upper/lower case,
    // and no account should be created
    @Test
    public void takenEmailIsRejectedIgnoringCase() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                .param("username", "someone")
                .param("email", "USER@example.com")
                .param("password", "secret123"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Email is already registered")));

        assertThat(userRepository.findByUsername("someone")).isNull();
    }

    // Breaks every validation rule in RegisterForm at once and checks that
    // all three error messages are shown and nothing is saved
    @Test
    public void invalidInputShowsValidationErrors() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                .param("username", "ab")
                .param("email", "not-an-email")
                .param("password", "123"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Username must be 3-30 characters")))
            .andExpect(content().string(containsString("Enter a valid email address")))
            .andExpect(content().string(containsString("Password must be 6-72 characters")));

        assertThat(userRepository.findByUsername("ab")).isNull();
    }
}
