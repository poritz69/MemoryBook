package memorybook.com.memorybook;

import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.Book;
import memorybook.com.memorybook.entity.ReadingStatus;
import memorybook.com.memorybook.repository.AppUserRepository;
import memorybook.com.memorybook.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookRepositoryTest {

    @Autowired private AppUserRepository appUserRepository;
    @Autowired private BookRepository bookRepository;

    @Test
    void fremdeBuecherWerdenNichtGefunden() {
        AppUser anna = saveUser("anna");
        AppUser bernd = saveUser("bernd");
        Book book = saveBook(anna, "Annas Buch", ReadingStatus.GEPLANT);

        assertTrue(bookRepository.findByIdAndOwner(book.getId(), anna).isPresent());
        assertTrue(bookRepository.findByIdAndOwner(book.getId(), bernd).isEmpty());
    }

    @Test
    void sucheFiltertNachTextUndStatus() {
        AppUser anna = saveUser("anna");
        saveBook(anna, "Der Hobbit", ReadingStatus.GELESEN);
        saveBook(anna, "Faust", ReadingStatus.LESE_ICH);

        assertEquals(1, bookRepository.search(anna, "%hobbit%", null, false, null, PageRequest.of(0, 12)).getTotalElements());
        assertEquals(1, bookRepository.search(anna, "%%", ReadingStatus.LESE_ICH, false, null, PageRequest.of(0, 12)).getTotalElements());
        assertEquals(2, bookRepository.search(anna, "%%", null, false, null, PageRequest.of(0, 12)).getTotalElements());
    }

    private AppUser saveUser(String name) {
        AppUser user = new AppUser();
        user.setUsername(name);
        user.setPassword("test");
        return appUserRepository.save(user);
    }

    private Book saveBook(AppUser owner, String title, ReadingStatus status) {
        Book book = new Book();
        book.setOwner(owner);
        book.setTitle(title);
        book.setStatus(status);
        return bookRepository.save(book);
    }
}
