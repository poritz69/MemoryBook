package memorybook.com.memorybook.repository;

import memorybook.com.memorybook.entity.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
}
