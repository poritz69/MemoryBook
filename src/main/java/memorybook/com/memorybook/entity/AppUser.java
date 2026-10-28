package memorybook.com.memorybook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Entity
@Table( name = "appuser")
public class AppUser {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id", nullable = false)
    private long id;

    @Column (name = "username", unique = true, nullable = false)
    private String username;

    @Column (name = "password")
    private String password;

    @OneToMany(mappedBy = "appuser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notes> notes;

}