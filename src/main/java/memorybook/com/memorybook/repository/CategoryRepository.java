package memorybook.com.memorybook.repository;

import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByOwnerOrderByNameAsc(AppUser owner);

    Optional<Category> findByIdAndOwner(Long id, AppUser owner);

    List<Category> findByOwnerAndIdIn(AppUser owner, List<Long> ids);

    boolean existsByOwnerAndNameIgnoreCase(AppUser owner, String name);
}
