package memorybook.com.memorybook.controller;

import jakarta.validation.Valid;
import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.Book;
import memorybook.com.memorybook.entity.Category;
import memorybook.com.memorybook.entity.ReadingStatus;
import memorybook.com.memorybook.repository.AppUserRepository;
import memorybook.com.memorybook.repository.BookRepository;
import memorybook.com.memorybook.repository.CategoryRepository;
import memorybook.com.memorybook.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BookController {

    @Autowired private BookRepository bookRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private NoteRepository noteRepository;
    @Autowired private AppUserRepository appUserRepository;

    // Startseite mit Übersicht
    @GetMapping("/")
    public String home(Principal principal, Model model) {
        AppUser user = currentUser(principal);
        LocalDate today = LocalDate.now();
        long finishedThisYear = bookRepository.countByOwnerAndStatusAndFinishedOnBetween(
                user, ReadingStatus.GELESEN, today.withDayOfYear(1), today.withDayOfYear(today.lengthOfYear()));

        // Anzahl Bücher pro Status
        Map<ReadingStatus, Long> statusCounts = new LinkedHashMap<>();
        for (ReadingStatus status : ReadingStatus.values()) {
            statusCounts.put(status, bookRepository.countByOwnerAndStatus(user, status));
        }

        model.addAttribute("user", user);
        model.addAttribute("totalBooks", bookRepository.countByOwner(user));
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("favorites", bookRepository.countByOwnerAndFavoriteTrue(user));
        model.addAttribute("pagesRead", bookRepository.sumPages(user, ReadingStatus.GELESEN));
        model.addAttribute("noteCount", noteRepository.countByOwner(user));
        model.addAttribute("finishedThisYear", finishedThisYear);
        model.addAttribute("goalPercent", Math.min(100, finishedThisYear * 100 / user.getReadingGoal()));
        model.addAttribute("reading", bookRepository.findByOwnerAndStatus(user, ReadingStatus.LESE_ICH));
        model.addAttribute("recentBooks", bookRepository.findTop4ByOwnerOrderByIdDesc(user));
        model.addAttribute("recentNotes", noteRepository.findTop5ByOwnerOrderByCreatedAtDesc(user));
        return "index";
    }

    // Alle Bücher anzeigen, mit Suche, Filter, Sortierung und Seiten
    @GetMapping("/books")
    public String getAllBooks(@RequestParam(defaultValue = "") String q,
                              @RequestParam(required = false) ReadingStatus status,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(defaultValue = "false") boolean favorite,
                              @RequestParam(defaultValue = "title") String sort,
                              @RequestParam(defaultValue = "0") int page,
                              Principal principal, Model model) {
        AppUser user = currentUser(principal);

        Sort order = switch (sort) {
            case "author" -> Sort.by("author");
            case "rating" -> Sort.by(Sort.Order.desc("rating").nullsLast());
            case "newest" -> Sort.by(Sort.Direction.DESC, "id");
            default -> Sort.by("title");
        };
        String text = "%" + q.trim().toLowerCase() + "%";
        Page<Book> books = bookRepository.search(user, text, status, favorite, categoryId,
                PageRequest.of(Math.max(page, 0), 12, order));

        model.addAttribute("books", books);
        model.addAttribute("categories", categoryRepository.findByOwnerOrderByNameAsc(user));
        model.addAttribute("statuses", ReadingStatus.values());
        // Filterwerte, damit das Formular sie wieder anzeigt
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("favorite", favorite);
        model.addAttribute("sort", sort);
        return "books/list";
    }

    // Hinzufügen eines neuen Buches anzeigen
    @GetMapping("/books/add")
    public String addPage(Principal principal, Model model) {
        model.addAttribute("book", new Book());
        addFormData(currentUser(principal), new ArrayList<>(), model);
        return "books/form";
    }

    // Buch bearbeiten
    @GetMapping("/books/edit/{id}")
    public String editPage(@PathVariable Long id, Principal principal, Model model) {
        AppUser user = currentUser(principal);
        Book book = findBook(id, user);
        List<Long> selected = new ArrayList<>();
        for (Category category : book.getCategories()) {
            selected.add(category.getId());
        }
        model.addAttribute("book", book);
        addFormData(user, selected, model);
        return "books/form";
    }

    // Buch speichern (neu oder bearbeitet)
    @PostMapping("/books/save")
    public String saveBook(@Valid @ModelAttribute("book") Book form, BindingResult result,
                           @RequestParam(required = false) List<Long> categoryIds,
                           Principal principal, Model model, RedirectAttributes redirect) {
        AppUser user = currentUser(principal);
        if (categoryIds == null) {
            categoryIds = new ArrayList<>();
        }
        if (form.getPages() != null && form.getCurrentPage() != null && form.getCurrentPage() > form.getPages()) {
            result.rejectValue("currentPage", "tooLarge", "Die Seite ist größer als die Seitenzahl.");
        }
        if (result.hasErrors()) {
            addFormData(user, categoryIds, model);
            return "books/form";
        }

        // Neues Buch anlegen oder das vorhandene laden (nur eigene Bücher)
        Book book = form.getId() == null ? new Book() : findBook(form.getId(), user);
        book.setOwner(user);
        book.setTitle(form.getTitle().trim());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        book.setPages(form.getPages());
        book.setCurrentPage(form.getCurrentPage());
        book.setDescription(form.getDescription());
        book.setStatus(form.getStatus());
        book.setRating(form.getRating());
        book.setStartedOn(form.getStartedOn());
        book.setFinishedOn(form.getFinishedOn());
        book.setFavorite(form.isFavorite());
        book.getCategories().clear();
        if (!categoryIds.isEmpty()) {
            book.getCategories().addAll(categoryRepository.findByOwnerAndIdIn(user, categoryIds));
        }
        updateDates(book);
        bookRepository.save(book);

        redirect.addFlashAttribute("message", "Buch gespeichert.");
        return "redirect:/books/" + book.getId();
    }

    // Ein Buch mit seinen Notizen anzeigen
    @GetMapping("/books/{id}")
    public String showBook(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("book", findBook(id, currentUser(principal)));
        return "books/detail";
    }

    // Buch löschen
    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        bookRepository.delete(findBook(id, currentUser(principal)));
        redirect.addFlashAttribute("message", "Buch gelöscht.");
        return "redirect:/books";
    }

    // Favoriten umschalten
    @PostMapping("/books/favorite/{id}")
    public String toggleFavorite(@PathVariable Long id, Principal principal) {
        Book book = findBook(id, currentUser(principal));
        book.setFavorite(!book.isFavorite());
        bookRepository.save(book);
        return "redirect:/books/" + id;
    }

    // Aktuelle Seite speichern
    @PostMapping("/books/progress/{id}")
    public String updateProgress(@PathVariable Long id, @RequestParam int currentPage, Principal principal) {
        Book book = findBook(id, currentUser(principal));
        int page = Math.max(0, currentPage);
        if (book.getPages() != null) {
            page = Math.min(page, book.getPages());
        }
        book.setCurrentPage(page);

        // Letzte Seite = gelesen, sonst liest man das Buch gerade
        if (book.getPages() != null && page == book.getPages()) {
            book.setStatus(ReadingStatus.GELESEN);
        } else if (page > 0) {
            book.setStatus(ReadingStatus.LESE_ICH);
        }
        updateDates(book);
        bookRepository.save(book);
        return "redirect:/books/" + id;
    }

    // Alle Bücher als CSV-Datei für Excel
    @GetMapping("/books/export")
    public ResponseEntity<String> exportBooks(Principal principal) {
        // Das erste Zeichen (BOM) sorgt dafür, dass Excel die Umlaute richtig anzeigt
        StringBuilder csv = new StringBuilder("\uFEFFTitel;Autor;ISBN;Seiten;Status;Bewertung;Favorit\n");
        for (Book book : bookRepository.findByOwnerOrderByTitleAsc(currentUser(principal))) {
            csv.append(csvValue(book.getTitle())).append(';')
                    .append(csvValue(book.getAuthor())).append(';')
                    .append(csvValue(book.getIsbn())).append(';')
                    .append(csvValue(book.getPages())).append(';')
                    .append(csvValue(book.getStatus() != null ? book.getStatus().getLabel() : null)).append(';')
                    .append(csvValue(book.getRating())).append(';')
                    .append(book.isFavorite() ? "ja" : "nein").append('\n');
        }
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=buecher.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.toString());
    }

    // Wert in Anführungszeichen, Anführungszeichen im Text werden verdoppelt
    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.toString().replace("\"", "\"\"") + "\"";
    }

    // Datum und Seite passend zum Status setzen
    private void updateDates(Book book) {
        if (book.getStatus() == ReadingStatus.LESE_ICH && book.getStartedOn() == null) {
            book.setStartedOn(LocalDate.now());
        }
        if (book.getStatus() == ReadingStatus.GELESEN) {
            if (book.getFinishedOn() == null) {
                book.setFinishedOn(LocalDate.now());
            }
            if (book.getPages() != null) {
                book.setCurrentPage(book.getPages());
            }
        } else {
            // Nur gelesene Bücher haben ein Enddatum
            book.setFinishedOn(null);
        }
    }

    // Kategorien und Status für das Buch-Formular
    private void addFormData(AppUser user, List<Long> selectedCategories, Model model) {
        model.addAttribute("categories", categoryRepository.findByOwnerOrderByNameAsc(user));
        model.addAttribute("selectedCategories", selectedCategories);
        model.addAttribute("statuses", ReadingStatus.values());
    }

    // Buch des Benutzers laden, sonst Fehler 404
    private Book findBook(Long id, AppUser user) {
        return bookRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private AppUser currentUser(Principal principal) {
        return appUserRepository.findByUsername(principal.getName());
    }
}
