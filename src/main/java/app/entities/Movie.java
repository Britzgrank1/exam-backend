package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "movies")

public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String title;
    LocalDate releaseDate;

    Double tmdbRating;
    @Column(name = "tmdb_id")
    Integer tmdbId;

    @ManyToMany
    @JoinTable(
            name = "movie_genre",joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "movie")
    Set<Credit> credits = new HashSet<>();

    @OneToMany(mappedBy = "movie")
    Set<UserMovieRating> ratings = new HashSet<>();

    @OneToMany(mappedBy = "movie")
    Set<Watchlist> watchlists = new HashSet<>();







}
