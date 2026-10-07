package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "genres")
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    Integer id;
    String name;

    @ManyToMany(mappedBy = "genres")
    Set<Movie> movies = new HashSet<>();



}

