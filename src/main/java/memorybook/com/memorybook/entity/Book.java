package memorybook.com.memorybook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table (name ="book" )
public class Book {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id", nullable = false)
    private long id;

    @Column (name = "titel")
    private String title;

    @Column (name = "favorite")
    @ColumnDefault("false")
    private boolean favorite;

    @Column (name = "author")
    private String author;

    @Column (name = "pages")
    private String pages;

    @Column (name = "beschreibung")
    private String beschreibung;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notes> notes = new ArrayList<>();

    @OneToMany(mappedBy = "book_id", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookCategory> categories = new ArrayList<>();


}
