package memorybook.com.memorybook.repository;


import memorybook.com.memorybook.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {


    Book getBookById(long id);



}