package memorybook.com.memorybook.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.ReadingStatus;
import memorybook.com.memorybook.repository.AppUserRepository;
import memorybook.com.memorybook.repository.BookRepository;
import memorybook.com.memorybook.repository.CategoryRepository;
import memorybook.com.memorybook.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;

@Controller
public class AppUserController {

    @Autowired private AppUserRepository appUserRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private NoteRepository noteRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // Login anzeigen
    @GetMapping("/login")
    public String getLoginView() {
        return "login";
    }

    // Registrierung anzeigen
    @GetMapping("/users")
    public String getRegisterView() {
        return "users";
    }

    // Benutzer speichern, Passwort wird mit BCrypt verschlüsselt
    @PostMapping("/saveUser")
    public String saveUser(@RequestParam String username, @RequestParam String password,
                           @RequestParam String passwordRepeat, Model model) {
        username = username.trim();
        String error = checkPassword(password, passwordRepeat);
        if (username.length() < 3) {
            error = "Der Benutzername muss mindestens 3 Zeichen haben.";
        } else if (appUserRepository.existsByUsername(username)) {
            error = "Dieser Benutzername ist schon vergeben.";
        }
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("username", username);
            return "users";
        }

        AppUser newUser = new AppUser();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        appUserRepository.save(newUser);
        return "redirect:/login?registered=true";
    }

    // Eigenes Profil mit ein paar Zahlen anzeigen
    @GetMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        AppUser user = appUserRepository.findByUsername(principal.getName());
        LocalDate today = LocalDate.now();

        model.addAttribute("user", user);
        model.addAttribute("totalBooks", bookRepository.countByOwner(user));
        model.addAttribute("readBooks", bookRepository.countByOwnerAndStatus(user, ReadingStatus.GELESEN));
        model.addAttribute("pagesRead", bookRepository.sumPages(user, ReadingStatus.GELESEN));
        model.addAttribute("noteCount", noteRepository.countByOwner(user));
        model.addAttribute("finishedThisYear", bookRepository.countByOwnerAndStatusAndFinishedOnBetween(
                user, ReadingStatus.GELESEN, today.withDayOfYear(1), today.withDayOfYear(today.lengthOfYear())));
        return "profile";
    }

    // Jahresziel ändern
    @PostMapping("/profile/goal")
    public String updateGoal(@RequestParam int readingGoal, Principal principal, RedirectAttributes redirect) {
        AppUser user = appUserRepository.findByUsername(principal.getName());
        user.setReadingGoal(Math.max(1, Math.min(readingGoal, 365)));
        appUserRepository.save(user);
        redirect.addFlashAttribute("message", "Jahresziel gespeichert.");
        return "redirect:/profile";
    }

    // Passwort ändern
    @PostMapping("/profile/password")
    public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword,
                                 @RequestParam String newPasswordRepeat, Principal principal,
                                 RedirectAttributes redirect) {
        AppUser user = appUserRepository.findByUsername(principal.getName());
        String error = checkPassword(newPassword, newPasswordRepeat);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            error = "Das aktuelle Passwort ist falsch.";
        }
        if (error != null) {
            redirect.addFlashAttribute("error", error);
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);
        redirect.addFlashAttribute("message", "Passwort geändert.");
        return "redirect:/profile";
    }

    // Konto mit allen Büchern, Notizen und Kategorien löschen (Passwort zur Sicherheit nochmal eingeben)
    @Transactional
    @PostMapping("/profile/delete")
    public String deleteAccount(@RequestParam String password, Principal principal, HttpServletRequest request,
                                RedirectAttributes redirect) throws ServletException {
        AppUser user = appUserRepository.findByUsername(principal.getName());
        if (!passwordEncoder.matches(password, user.getPassword())) {
            redirect.addFlashAttribute("error", "Das Passwort ist falsch, das Konto wurde nicht gelöscht.");
            return "redirect:/profile";
        }

        // Notizen werden mit den Büchern gelöscht
        bookRepository.deleteAll(bookRepository.findByOwnerOrderByTitleAsc(user));
        categoryRepository.deleteAll(categoryRepository.findByOwnerOrderByNameAsc(user));
        appUserRepository.delete(user);
        request.logout();
        return "redirect:/login?deleted=true";
    }

    // Gibt eine Fehlermeldung zurück oder null, wenn das Passwort passt
    private String checkPassword(String password, String passwordRepeat) {
        if (password.length() < 8) {
            return "Das Passwort muss mindestens 8 Zeichen haben.";
        }
        // BCrypt kann nur 72 Bytes, Umlaute zählen doppelt
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return "Das Passwort ist zu lang.";
        }
        if (!password.equals(passwordRepeat)) {
            return "Die Passwörter stimmen nicht überein.";
        }
        return null;
    }
}
