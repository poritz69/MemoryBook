package memorybook.com.memorybook.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // Spalte heißt noch "titel" wie in der ersten Version
    @NotBlank(message = "Bitte einen Titel eingeben.")
    @Size(max = 200, message = "Der Titel ist zu lang.")
    @Column(name = "titel")
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "isbn")
    private String isbn;

    @Min(value = 1, message = "Die Seitenzahl muss mindestens 1 sein.")
    @Column(name = "page_count")
    private Integer pages;

    @Min(value = 0, message = "Die Seite darf nicht negativ sein.")
    @Column(name = "current_page")
    private Integer currentPage;

    @Size(max = 4000, message = "Die Beschreibung ist zu lang.")
    @Column(name = "description", length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReadingStatus status = ReadingStatus.GEPLANT;

    // 1 bis 5 Sterne, null = nicht bewertet
    @Min(1)
    @Max(5)
    @Column(name = "rating")
    private Integer rating;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "started_on")
    private LocalDate startedOn;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "finished_on")
    private LocalDate finishedOn;

    @Column(name = "favorite")
    @ColumnDefault("false")
    private boolean favorite;

    // Wem das Buch gehört
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @ManyToMany
    @JoinTable(name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    // Notizen werden mit dem Buch gelöscht
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Note> notes = new ArrayList<>();

    // Fortschritt in Prozent für den Balken
    public int getProgressPercent() {
        if (status == ReadingStatus.GELESEN) {
            return 100;
        }
        if (pages == null || currentPage == null || pages == 0) {
            return 0;
        }
        return Math.min(100, currentPage * 100 / pages);
    }

    // Cover von Open Library über die ISBN
    public String getCoverUrl() {
        if (isbn == null || isbn.isBlank()) {
            return null;
        }
        return "https://covers.openlibrary.org/b/isbn/" + isbn.replace("-", "") + "-M.jpg?default=false";
    }
}
