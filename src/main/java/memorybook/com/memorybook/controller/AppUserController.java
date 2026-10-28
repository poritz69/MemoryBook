package memorybook.com.memorybook.controller;

import jakarta.servlet.http.HttpSession;
import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class AppUserController {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



    //Benutzer und Paawort verschlüssen
    @PostMapping("/saveUser")
    public String saveUser(@RequestParam String username, @RequestParam String password, Model model) {
        if (appUserRepository.existsByUsername(username)) {
            model.addAttribute("error", "Benutzername ist schon vergeben.");
            return "users";
        }
        AppUser newUser = new AppUser();
        newUser.setUsername(username);
        // Password wird mit BCrypt verschlüsselt vor dem Speichern:
        newUser.setPassword(passwordEncoder.encode(password));

        appUserRepository.save(newUser); // Kleingeschriebenes Objekt verwenden!
        return "redirect:/login";
    }

    // Registrierung anzeigen
    @GetMapping({"/users", "/register"})
    public String showRegister() {
        return "users";
    }

    // Login anzeigen
    @GetMapping("/login")
    public String getLoginView() {
        return "login";
    }
    // Eigenes Profil des aktuell angemeldeten Benutzers anzeigen
    @GetMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        // liefert den Benutzernamen des aktuell  User
        String username = principal.getName();
        AppUser user = appUserRepository.findByUsername(username);

        model.addAttribute("user", user);
        return "profile";


    }
}

