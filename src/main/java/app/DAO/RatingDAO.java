package app.DAO;

import app.entities.Movie;
import app.entities.User;
import app.entities.UserMovieRating;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Set;

public class RatingDAO {
    private final EntityManagerFactory emf;

    public RatingDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public UserMovieRating rateMovie(String username, Integer movieId, Integer rating, String comment) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                // Finder brugeren
                User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                        .setParameter("username", username)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);
                if (user == null) {
                    throw new ApiException(404, "User not found");

                }
                //Prøver at finde filmen
                Movie movie = em.find(Movie.class, movieId);
                if (movie == null) {
                    throw new ApiException(404, "Movie with id" + movieId + "not found");
                }
                //Ser om en user allerede har vurderet
                UserMovieRating existing = em.createQuery("SELECT r FROM UserMovieRating r WHERE r.users" +
                                " = :user AND r.movie = :movie", UserMovieRating.class)
                        .setParameter("user", user)
                        .setParameter("movie", movie)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);
                //
                if (existing == null) {
                    existing = new UserMovieRating();
                    existing.setUsers(user);
                    existing.setMovie(movie);
                    em.persist(existing);
                }
                existing.setRating(rating);
                existing.setComment(comment);

                em.getTransaction().commit();
                return existing;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }

    // Her prøver jeg at finde de film user har givet mindst minRating og henter resultaterne som hvilken film og genre det var
    public List<UserMovieRating> getLikedRatings(String username, int minRating) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT DISTINCT r FROM UserMovieRating r " +
                            "Join FETCH r.movie m " + "LEFT JOIN FETCH m.genres "
                            + "WHERE r.users.username = :username AND r.rating >= :minRating", UserMovieRating.class)
                    .setParameter("username", username)
                    .setParameter("minRating", minRating)
                    .getResultList();
        }
    }

    //Her kigger jeg på film i de genre som brugeren ikke har vurderet endnu og kigger på bedst TMDB-rating
    public List<Movie> getMoviesInGenresNotRatedBy(String username, Set<Integer> genreIds) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT DISTINCT m FROM Movie m " +
                            "LEFT Join FETCH m.genres "
                            + "WHERE m.id IN (SELECT m2.id FROM Movie m2 JOIN m2.genres g WHERE g.id IN :genreIds) "
                            + "AND m.id NOT IN (SELECT r.movie.id FROM UserMovieRating r WHERE r.users.username = :username)"
                            + "ORDER BY m.tmdbRating DESC", Movie.class)
                    .setParameter("username", username)
                    .setParameter("genreIds", genreIds)
                    .getResultList();
        }

    }
}
