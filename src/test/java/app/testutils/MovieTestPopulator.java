package app.testutils;

import app.entities.Genre;
import app.entities.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MovieTestPopulator {

    private MovieTestPopulator() {}

    public static Map<String, Movie> populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                // Tøm tabellerne og start id'er forfra ved 1
                em.createNativeQuery("TRUNCATE TABLE movies, genres RESTART IDENTITY CASCADE").executeUpdate();

                Genre action = genre("Action", 28);
                Genre drama = genre("Drama", 18);
                em.persist(action);
                em.persist(drama);

                Movie movie1 = movie("Test Movie 1", LocalDate.of(2020, 1, 1), 7.5, 1001, action);
                Movie movie2 = movie("Test Movie 2", LocalDate.of(2021, 2, 2), 8.0, 1002, action, drama);
                Movie movie3 = movie("Test Movie 3", LocalDate.of(2022, 3, 3), 6.0, 1003, drama);
                em.persist(movie1);
                em.persist(movie2);
                em.persist(movie3);

                em.getTransaction().commit();

                Map<String, Movie> seeded = new LinkedHashMap<>();
                seeded.put("movie1", movie1);
                seeded.put("movie2", movie2);
                seeded.put("movie3", movie3);
                return seeded;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }

    private static Genre genre(String name, int tmdbId) {
        Genre genre = new Genre();
        genre.setName(name);
        genre.setTmdbId(tmdbId);
        return genre;
    }

    private static Movie movie(String title, LocalDate releaseDate, double tmdbRating, int tmdbId, Genre... genres) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setReleaseDate(releaseDate);
        movie.setTmdbRating(tmdbRating);
        movie.setTmdbId(tmdbId);
        for (Genre g : genres) {
            movie.getGenres().add(g);
        }
        return movie;
    }
}