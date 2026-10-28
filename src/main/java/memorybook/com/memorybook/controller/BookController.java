package memorybook.com.memorybook.controller;

import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.Book;
import memorybook.com.memorybook.entity.Category;
import memorybook.com.memorybook.repository.AppUserRepository;
import memorybook.com.memorybook.repository.BookRepository;
import memorybook.com.memorybook.repository.CategoryRepository;
import memorybook.com.memorybook.repository.NotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Controller
public class BookController {

    @Autowired private BookRepository bookRepository;
    @Autowired private NotesRepository notesRepository;
    @Autowired private AppUserRepository appUserRepository;
    @Autowired private CategoryRepository categoryRepository;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Alle Bücher anzeigen
    @GetMapping("/books")
    public String getAllBooks(Model model) {
        List<Book> books = bookRepository.findAll();
        model.addAttribute("books", books);
        return "book-list";
    }

    // Hinzufügen eines neuen Buches anzeigen
    @GetMapping("/books/add")
    public String addPage(Model model) {
        model.addAttribute("book", new Book());
        return "new_book";
    }


    // Buch speichern
    @PostMapping("/books/save")
    public String saveBook(@ModelAttribute Book book) {
        bookRepository.save(book);
        return "redirect:/books";
    }

    // Buch löschen
    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookRepository.deleteById(id);
        return "redirect:/books";
    }

    // Favoriten umschalten
    @PostMapping("/books/favorite/{id}")
    public String toggleFavorite(@PathVariable Long id) {
        bookRepository.findById(id).ifPresent(book -> {
            book.setFavorite(!book.isFavorite());
            bookRepository.save(book);
        });
        return "redirect:/books";
    }

    // Alle Kategorien anzeigen
    @GetMapping("/categories")
    public String allCategory(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "category-list";
    }

    // Ermöglicht Suche nach spezifischer Kategorie
    @GetMapping("/categories/{id}")
    public String getCategoryById(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("category", category);
        return "category";
    }



    // Kategorie anlegen
    @PostMapping("/save/category-list")
    public String createCategory(@RequestParam String name) {
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
        return "redirect:/categories";
    }


}
