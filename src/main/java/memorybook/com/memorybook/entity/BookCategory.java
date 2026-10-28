package memorybook.com.memorybook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table (name ="BookCategory")
public class BookCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book_id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category_id;
}