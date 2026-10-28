package memorybook.com.memorybook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table (name ="Category" )
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "name")
    private String name;

    //@Column(name = "Thriller")
    //private String thriller;

    //@Column(name = "Horror")
    //private String horror;

    //@Column(name = "Fantasy")
    //private String fantasy;

    //@Column(name = "Romance")
    //private String romance;

}
