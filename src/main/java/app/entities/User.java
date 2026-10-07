package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Table(name = "users")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String username;
    String email;

    @Column(name = "password_hash")
    String password;

    @OneToMany(mappedBy = "users")
    Set<UserMovieRating> ratings = new HashSet<>();

    @OneToMany(mappedBy = "users")
    Set<Watchlist> watchlists = new HashSet<>();

}
