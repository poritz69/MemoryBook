package memorybook.com.memorybook.repository;

import memorybook.com.memorybook.entity.Book;
import memorybook.com.memorybook.entity.Notes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotesRepository extends JpaRepository<Notes, Long> {

    List<Notes> findAllByBook(Book book);

    List<Notes> findAllByBookId(Long id);

}