package memorybook.com.memorybook.config;

import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.Book;
import memorybook.com.memorybook.entity.Category;
import memorybook.com.memorybook.entity.Note;
import memorybook.com.memorybook.entity.ReadingStatus;
import memorybook.com.memorybook.repository.AppUserRepository;
import memorybook.com.memorybook.repository.BookRepository;
import memorybook.com.memorybook.repository.CategoryRepository;
import memorybook.com.memorybook.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// Testdaten zum Ausprobieren: Benutzer "demo" mit Passwort "demo1234"
// Starten mit: ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
@Component
@Profile("demo")
public class DemoDataLoader implements CommandLineRunner {

    @Autowired private AppUserRepository appUserRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private NoteRepository noteRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (appUserRepository.existsByUsername("demo")) {
            return;
        }

        AppUser demo = new AppUser();
        demo.setUsername("demo");
        demo.setPassword(passwordEncoder.encode("demo1234"));
        appUserRepository.save(demo);

        Category fantasy = addCategory(demo, "Fantasy");
        Category classic = addCategory(demo, "Klassiker");
        Category science = addCategory(demo, "Sachbuch");
        addCategory(demo, "Krimi");

        LocalDate today = LocalDate.now();
        Book hobbit = addBook(demo, "Der kleine Hobbit", "J. R. R. Tolkien", "9783423715669", 384,
                ReadingStatus.GELESEN, 384, 5, true, fantasy);
        hobbit.getCategories().add(classic);
        hobbit.setStartedOn(today.minusMonths(2));
        hobbit.setFinishedOn(today.minusMonths(1));
        bookRepository.save(hobbit);
        addNote(demo, hobbit, "In einer Höhle in der Erde, da lebte ein Hobbit.", 1);
        addNote(demo, hobbit, "Das Rätselspiel mit Gollum ist das beste Kapitel.", 87);

        Book faust = addBook(demo, "Faust", "Johann Wolfgang von Goethe", "9783150000014", 160,
                ReadingStatus.LESE_ICH, 64, null, false, classic);
        addNote(demo, faust, "Da steh ich nun, ich armer Tor! Und bin so klug als wie zuvor.", 15);

        addBook(demo, "Eine kurze Geschichte der Menschheit", "Yuval Noah Harari", "9783570552698", 528,
                ReadingStatus.LESE_ICH, 120, null, false, science);
        addBook(demo, "Der Name des Windes", "Patrick Rothfuss", "9783608938159", 864,
                ReadingStatus.GEPLANT, null, null, false, fantasy);
        addBook(demo, "Kurze Antworten auf große Fragen", "Stephen Hawking", "9783608963762", 256,
                ReadingStatus.GELESEN, 256, 4, false, science);
        addBook(demo, "Ulysses", "James Joyce", null, 1000,
                ReadingStatus.ABGEBROCHEN, 150, 2, false, classic);
    }

    private Category addCategory(AppUser owner, String name) {
        Category category = new Category();
        category.setName(name);
        category.setOwner(owner);
        return categoryRepository.save(category);
    }

    private Book addBook(AppUser owner, String title, String author, String isbn, Integer pages,
                         ReadingStatus status, Integer currentPage, Integer rating, boolean favorite, Category category) {
        Book book = new Book();
        book.setOwner(owner);
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPages(pages);
        book.setStatus(status);
        book.setCurrentPage(currentPage);
        book.setRating(rating);
        book.setFavorite(favorite);
        book.getCategories().add(category);
        if (status != ReadingStatus.GEPLANT) {
            book.setStartedOn(LocalDate.now().minusWeeks(3));
        }
        if (status == ReadingStatus.GELESEN) {
            book.setFinishedOn(LocalDate.now().minusWeeks(1));
        }
        return bookRepository.save(book);
    }

    private void addNote(AppUser owner, Book book, String content, Integer page) {
        Note note = new Note();
        note.setOwner(owner);
        note.setBook(book);
        note.setContent(content);
        note.setPage(page);
        noteRepository.save(note);
    }
}
