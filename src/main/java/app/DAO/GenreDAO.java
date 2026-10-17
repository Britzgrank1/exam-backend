package app.DAO;

import app.entities.Genre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class GenreDAO {

    private final EntityManagerFactory emf;

    public GenreDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Genre getById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Genre.class,id);
        }
    }

    public List<Genre> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT g FROM Genre g", Genre.class).getResultList();
        }
    }

}