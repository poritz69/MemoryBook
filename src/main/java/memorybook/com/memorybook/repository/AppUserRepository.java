package memorybook.com.memorybook.repository;

import memorybook.com.memorybook.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    AppUser findByUsername(String username);

    boolean existsByUsername(String username);
}
