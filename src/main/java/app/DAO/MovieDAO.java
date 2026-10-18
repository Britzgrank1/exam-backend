package app.DAO;


import app.entities.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class MovieDAO {

    private final EntityManagerFactory emf;

    public MovieDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Movie create(Movie movie) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(movie);
            em.getTransaction().commit();
            return movie;
        }
    }

    public Movie getById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM Movie m LEFT JOIN FETCH m.genres WHERE m.id = :id", Movie.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

    public List<Movie> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres", Movie.class).getResultList();
        }
    }

    public Movie update(Movie movie) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Movie merged = em.merge(movie);
            em.getTransaction().commit();
            return merged;
        }
    }

    public void delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Movie movie = em.find(Movie.class, id);
            if (movie != null) {
                em.remove(movie);
            }
            em.getTransaction().commit();
        }
    }
    public Movie getByTmdbId(Integer tmdbId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM Movie m WHERE m.tmdbId = :tmdbId", Movie.class)
                    .setParameter("tmdbId", tmdbId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }
}