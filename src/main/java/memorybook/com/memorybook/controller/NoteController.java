package memorybook.com.memorybook.controller;

import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.Book;
import memorybook.com.memorybook.entity.Note;
import memorybook.com.memorybook.repository.AppUserRepository;
import memorybook.com.memorybook.repository.BookRepository;
import memorybook.com.memorybook.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class NoteController {

    @Autowired private NoteRepository notesRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private AppUserRepository appUserRepository;

    // Alle eigenen Notizen, optional mit Suche
    @GetMapping("/notes")
    public String allNotes(@RequestParam(defaultValue = "") String q, Principal principal, Model model) {
        model.addAttribute("notes", notesRepository
                .findByOwnerAndContentContainingIgnoreCaseOrderByCreatedAtDesc(currentUser(principal), q.trim()));
        model.addAttribute("q", q);
        return "notes/list";
    }

    // Neue Notiz zu einem Buch speichern
    @PostMapping("/books/{id}/notes/save")
    public String saveNote(@PathVariable Long id, @RequestParam String content,
                           @RequestParam(required = false) Integer page, Principal principal,
                           RedirectAttributes redirect) {
        AppUser user = currentUser(principal);
        Book book = bookRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (content.isBlank()) {
            redirect.addFlashAttribute("error", "Die Notiz darf nicht leer sein.");
            return "redirect:/books/" + id;
        }

        Note note = new Note();
        note.setContent(content.trim());
        note.setPage(page);
        note.setBook(book);
        note.setOwner(user);
        notesRepository.save(note);
        return "redirect:/books/" + id;
    }

    // Notiz bearbeiten
    @GetMapping("/notes/edit/{id}")
    public String editNote(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("note", findNote(id, currentUser(principal)));
        return "notes/edit";
    }

    @PostMapping("/notes/update/{id}")
    public String updateNote(@PathVariable Long id, @RequestParam String content,
                             @RequestParam(required = false) Integer page, Principal principal,
                             RedirectAttributes redirect) {
        Note note = findNote(id, currentUser(principal));
        if (content.isBlank()) {
            redirect.addFlashAttribute("error", "Die Notiz darf nicht leer sein.");
            return "redirect:/notes/edit/" + id;
        }
        note.setContent(content.trim());
        note.setPage(page);
        notesRepository.save(note);
        return "redirect:/books/" + note.getBook().getId();
    }

    //  Notiz löschen
    @PostMapping("/notes/delete/{id}")
    public String deleteNote(@PathVariable Long id, Principal principal) {
        Note note = findNote(id, currentUser(principal));
        Long bookId = note.getBook().getId();
        notesRepository.delete(note);
        return "redirect:/books/" + bookId;
    }

    private Note findNote(Long id, AppUser user) {
        return notesRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private AppUser currentUser(Principal principal) {
        return appUserRepository.findByUsername(principal.getName());
    }
}
