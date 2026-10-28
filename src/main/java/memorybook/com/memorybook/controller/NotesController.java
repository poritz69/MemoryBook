package memorybook.com.memorybook.controller;

import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.Book;
import memorybook.com.memorybook.entity.Notes;
import memorybook.com.memorybook.repository.AppUserRepository;
import memorybook.com.memorybook.repository.BookRepository;
import memorybook.com.memorybook.repository.NotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class NotesController {


     @Autowired private NotesRepository notesRepository;
     @Autowired private  BookRepository bookRepository;
     @Autowired private AppUserRepository appUserRepository;

    // Notizen für ein spezifisches Buch anzeigen
        @GetMapping("/books/{id}/notes")
        public String showNotes(@PathVariable Long id, Model model) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("book", book);
        model.addAttribute("notes", notesRepository.findAllByBookId(id));
        return "book-notes";
        }

    //  Neue Notiz speichern
        @PostMapping("/books/{id}/notes/save")
        public String saveNote(@PathVariable Long id, @RequestParam String content, Principal p) {
        Notes notes = new Notes();
        notes.setContent(content);
        notes.setBook(bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        notes.setDate(LocalDate.now());
        notes.setAppuser(appUserRepository.findByUsername(p.getName()));

        notesRepository.save(notes);
        return "redirect:/books/" + id + "/notes";
    }

    //  Notiz löschen
    @PostMapping("/notes/delete/{id}")
    public String deleteNote(@PathVariable Long id, @RequestParam Long bookId) {
        notesRepository.deleteById(id);
        return "redirect:/books/" + bookId + "/notes";
    }
}
