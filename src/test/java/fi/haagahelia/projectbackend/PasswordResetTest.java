package fi.haagahelia.projectbackend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import fi.haagahelia.projectbackend.model.AppUser;
import fi.haagahelia.projectbackend.model.PasswordResetToken;
import fi.haagahelia.projectbackend.repository.PasswordResetTokenRepository;
import fi.haagahelia.projectbackend.repository.UserRepository;

// The mail sender is mocked, so no real email is sent and we can inspect the message
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PasswordResetTest {

    private static final String SENT_MESSAGE = "If an account with that email exists";
    private static final String INVALID_LINK = "This reset link is invalid or has expired.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Fake mail sender: records what would be sent instead of sending it
    @MockitoBean
    private JavaMailSender mailSender;

    private AppUser user;

    // Fresh test user before each test (rolled back afterwards by @Transactional)
    @BeforeEach
    public void createUser() {
        user = userRepository.save(new AppUser("resetuser", passwordEncoder.encode("oldpassword"),
                "reset@example.com", "USER"));
    }

    // Helper: creates a reset token for the test user with the given expiry time
    private PasswordResetToken saveToken(String token, LocalDateTime expiry) {
        return tokenRepository.save(new PasswordResetToken(token, user, expiry));
    }

    // Asking for a reset with a registered email (any case, extra spaces) creates
    // a token that hasn't expired and emails a link containing that token.
    // ArgumentCaptor grabs the email that was passed to the fake mail sender.
    @Test
    public void knownEmailCreatesTokenAndSendsLink() throws Exception {
        mockMvc.perform(post("/forgot-password").with(csrf()).param("email", " RESET@example.com "))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(SENT_MESSAGE)));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage mail = captor.getValue();

        PasswordResetToken token = tokenRepository.findAll().iterator().next();
        assertThat(token.getUser().getId()).isEqualTo(user.getId());
        assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now());
        assertThat(mail.getTo()).containsExactly("reset@example.com");
        assertThat(mail.getText()).contains("/reset-password?token=" + token.getToken());
    }

    // A user only ever has one active token: a new request deletes the old one
    @Test
    public void newRequestReplacesOldToken() throws Exception {
        saveToken("old-token", LocalDateTime.now().plusMinutes(30));

        mockMvc.perform(post("/forgot-password").with(csrf()).param("email", "reset@example.com"))
            .andExpect(status().isOk());

        assertThat(tokenRepository.findByToken("old-token")).isNull();
        assertThat(tokenRepository.count()).isEqualTo(1);
    }

    // An unknown email gets the same message as a known one, so the form doesn't
    // reveal which emails are registered; no email is sent and no token is made
    @Test
    public void unknownEmailShowsSameMessageButSendsNothing() throws Exception {
        mockMvc.perform(post("/forgot-password").with(csrf()).param("email", "nobody@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(SENT_MESSAGE)));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        assertThat(tokenRepository.count()).isZero();
    }

    // Opening the link from the email with a valid token shows the new-password form
    @Test
    public void validTokenShowsResetForm() throws Exception {
        saveToken("valid-token", LocalDateTime.now().plusMinutes(30));

        mockMvc.perform(get("/reset-password").param("token", "valid-token"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Save password")));
    }

    // A made-up token shows the "invalid or expired" message instead of the form
    @Test
    public void unknownTokenIsRejected() throws Exception {
        mockMvc.perform(get("/reset-password").param("token", "does-not-exist"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(INVALID_LINK)));
    }

    // A token that expired a minute ago is refused and cleaned up from the database
    @Test
    public void expiredTokenIsRejectedAndDeleted() throws Exception {
        saveToken("expired-token", LocalDateTime.now().minusMinutes(1));

        mockMvc.perform(get("/reset-password").param("token", "expired-token"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(INVALID_LINK)));

        assertThat(tokenRepository.findByToken("expired-token")).isNull();
    }

    // Happy path: the new password is saved (hashed), the token is used up
    // so the link can't be reused, and the user is sent to the login page
    @Test
    public void resetChangesPasswordAndDeletesToken() throws Exception {
        saveToken("valid-token", LocalDateTime.now().plusMinutes(30));

        mockMvc.perform(post("/reset-password").with(csrf())
                .param("token", "valid-token")
                .param("password", "newpassword")
                .param("confirmPassword", "newpassword"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?reset"));

        AppUser updated = userRepository.findByUsername("resetuser");
        assertThat(passwordEncoder.matches("newpassword", updated.getPasswordHash())).isTrue();
        assertThat(tokenRepository.findByToken("valid-token")).isNull();
    }

    // If the two password fields differ, show an error and change nothing:
    // the old password still works and the token stays so the user can retry
    @Test
    public void mismatchedPasswordsAreRejected() throws Exception {
        saveToken("valid-token", LocalDateTime.now().plusMinutes(30));

        mockMvc.perform(post("/reset-password").with(csrf())
                .param("token", "valid-token")
                .param("password", "newpassword")
                .param("confirmPassword", "different"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Passwords do not match")));

        AppUser unchanged = userRepository.findByUsername("resetuser");
        assertThat(passwordEncoder.matches("oldpassword", unchanged.getPasswordHash())).isTrue();
        assertThat(tokenRepository.findByToken("valid-token")).isNotNull();
    }

    // Posting the form directly with a fake token must not change anyone's password
    @Test
    public void resetWithInvalidTokenDoesNothing() throws Exception {
        mockMvc.perform(post("/reset-password").with(csrf())
                .param("token", "does-not-exist")
                .param("password", "newpassword")
                .param("confirmPassword", "newpassword"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(INVALID_LINK)));

        AppUser unchanged = userRepository.findByUsername("resetuser");
        assertThat(passwordEncoder.matches("oldpassword", unchanged.getPasswordHash())).isTrue();
    }
}
