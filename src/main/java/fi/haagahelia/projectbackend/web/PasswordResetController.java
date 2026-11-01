package fi.haagahelia.projectbackend.web;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import fi.haagahelia.projectbackend.model.AppUser;
import fi.haagahelia.projectbackend.model.PasswordResetToken;
import fi.haagahelia.projectbackend.repository.PasswordResetTokenRepository;
import fi.haagahelia.projectbackend.repository.UserRepository;
import jakarta.validation.Valid;

@Controller
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
    private static final int TOKEN_VALID_MINUTES = 30;
    private static final String INVALID_LINK = "This reset link is invalid or has expired.";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String mailFrom;

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam("email") String email, Model model) {
        AppUser user = userRepository.findByEmailIgnoreCase(email.trim());

        if (user == null) {
            log.info("Password reset requested for unknown email {}", email);
        } else {
            // Only one active token per user
            tokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            tokenRepository.save(new PasswordResetToken(token, user,
                    LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES)));

            String link = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/reset-password")
                    .queryParam("token", token)
                    .toUriString();
            sendResetEmail(user.getEmail(), link);
        }

        // Same message either way, so the form doesn't reveal which emails are registered
        model.addAttribute("message", "If an account with that email exists, a reset link has been sent.");
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam(name = "token", required = false) String token, Model model) {
        if (findValidToken(token) == null) {
            model.addAttribute("error", INVALID_LINK);
            return "reset-password";
        }
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken(token);
        model.addAttribute("resetPasswordForm", form);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
            BindingResult bindingResult, Model model) {
        PasswordResetToken resetToken = findValidToken(form.getToken());
        if (resetToken == null) {
            model.addAttribute("error", INVALID_LINK);
            return "reset-password";
        }

        if (!bindingResult.hasFieldErrors("password")
                && !form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }
        if (bindingResult.hasErrors()) {
            return "reset-password";
        }

        AppUser user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);
        tokenRepository.delete(resetToken);

        return "redirect:/login?reset";
    }

    // Returns the token if it exists and hasn't expired. Expired tokens are deleted.
    private PasswordResetToken findValidToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null) {
            return null;
        }
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            return null;
        }
        return resetToken;
    }

    private void sendResetEmail(String to, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("Password reset");
        message.setText("Click the link below to reset your password. It is valid for "
                + TOKEN_VALID_MINUTES + " minutes.\n\n" + link
                + "\n\nIf you didn't request this, you can ignore this email.");
        if (mailFrom == null || mailFrom.isBlank()) {
            log.warn("No sender address (app.mail.from) is set, so no email was sent. Reset link for {}: {}", to, link);
            return;
        }
        try {
            mailSender.send(message);
            log.info("Password reset email sent from {} to {}", mailFrom, to);
        } catch (MailException e) {
            log.error("Could not send password reset email to {}", to, e);
        }
    }
}
