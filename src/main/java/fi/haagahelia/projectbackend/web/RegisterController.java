package fi.haagahelia.projectbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import fi.haagahelia.projectbackend.model.AppUser;
import fi.haagahelia.projectbackend.repository.UserRepository;
import jakarta.validation.Valid;

@Controller
public class RegisterController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form, BindingResult bindingResult) {
        String username = form.getUsername() == null ? "" : form.getUsername().trim();
        String email = form.getEmail() == null ? "" : form.getEmail().trim().toLowerCase();

        if (!bindingResult.hasFieldErrors("username") && userRepository.existsByUsername(username)) {
            bindingResult.rejectValue("username", "username.taken", "Username is already taken");
        }
        if (!bindingResult.hasFieldErrors("email") && userRepository.existsByEmailIgnoreCase(email)) {
            bindingResult.rejectValue("email", "email.taken", "Email is already registered");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        String hash = passwordEncoder.encode(form.getPassword());
        userRepository.save(new AppUser(username, hash, email, "USER"));
        return "redirect:/login?registered";
    }
}
