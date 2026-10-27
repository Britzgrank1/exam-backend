package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "user_movie_ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","movie_id"}))
@Entity

public class UserMovieRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User users;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    Movie movie;


    Integer rating;
    String comment;


}
