package memorybook.com.memorybook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "appuser")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password")
    private String password;

    // Jahresziel: so viele Bücher will man dieses Jahr lesen
    @ColumnDefault("12")
    @Column(name = "reading_goal", nullable = false)
    private int readingGoal = 12;
}
