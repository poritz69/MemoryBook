package memorybook.com.memorybook.repository;

import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.Book;
import memorybook.com.memorybook.entity.ReadingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Buch nur finden, wenn es dem Benutzer gehört
    Optional<Book> findByIdAndOwner(Long id, AppUser owner);

    List<Book> findByOwnerOrderByTitleAsc(AppUser owner);

    List<Book> findByOwnerAndStatus(AppUser owner, ReadingStatus status);

    List<Book> findTop4ByOwnerOrderByIdDesc(AppUser owner);

    long countByOwner(AppUser owner);

    long countByOwnerAndStatus(AppUser owner, ReadingStatus status);

    long countByOwnerAndFavoriteTrue(AppUser owner);

    long countByOwnerAndStatusAndFinishedOnBetween(AppUser owner, ReadingStatus status, LocalDate from, LocalDate to);

    @Query("select coalesce(sum(b.pages), 0) from Book b where b.owner = :owner and b.status = :status")
    long sumPages(@Param("owner") AppUser owner, @Param("status") ReadingStatus status);

    // Suche für die Bücherliste, Filter die null sind werden ignoriert
    @Query("""
            select b from Book b
            where b.owner = :owner
              and (lower(b.title) like :text or lower(b.author) like :text)
              and (:status is null or b.status = :status)
              and (:favorite = false or b.favorite = true)
              and (:categoryId is null or exists (
                    select c.id from Category c join c.books cb where c.id = :categoryId and cb = b))
            """)
    Page<Book> search(@Param("owner") AppUser owner,
                      @Param("text") String text,
                      @Param("status") ReadingStatus status,
                      @Param("favorite") boolean favorite,
                      @Param("categoryId") Long categoryId,
                      Pageable pageable);
}
