package memorybook.com.memorybook.repository;

import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByIdAndOwner(Long id, AppUser owner);

    List<Note> findTop5ByOwnerOrderByCreatedAtDesc(AppUser owner);

    List<Note> findByOwnerAndContentContainingIgnoreCaseOrderByCreatedAtDesc(AppUser owner, String text);

    long countByOwner(AppUser owner);
}
