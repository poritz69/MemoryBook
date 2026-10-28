package memorybook.com.memorybook.repository;

import memorybook.com.memorybook.entity.Book;
import memorybook.com.memorybook.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
