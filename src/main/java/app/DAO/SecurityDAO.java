package app.DAO;

import app.DTO.UserDTO;
import app.entities.Role;
import app.entities.User;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class SecurityDAO {

    private final EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Load the user from the database with their roles.
     * Check the password using user.verifyPassword(password).
     * Return the user if valid; otherwise throw an exception for invalid credentials.
     * */
    public UserDTO getVerifiedUser(String username, String password) {
        try (EntityManager em = emf.createEntityManager()) {

            User user = findByUsername(em, username);

            if (user == null || !user.verifyPassword(password)) {
                throw new ApiException(401, "Invalid credentials");
            }

            return new UserDTO(
                    user.getUsername(),
                    user.getRolesAsStrings()
            );
        }
    }
    /**
     * Check that the username isn’t already taken.
     * Create the user with a hashed password—your User constructor may already handle that.
     * Assign the default "user" role.
     * Persist and return the user.
     * */
    public UserDTO createUser(String username, String password)
    {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            try {
                if (findByUsername(em, username) != null) {
                    throw new ApiException(409, "Username already taken");
                }

                Role role = em.find(Role.class, "user");
                if (role == null) {
                    role = new Role("user");
                    em.persist(role);
                }

                User user = new User(username, password);
                user.addRole(role);
                em.persist(user);

                em.getTransaction().commit();

                return new UserDTO(
                        user.getUsername(),
                        user.getRolesAsStrings()
                );
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }
    private User findByUsername(EntityManager em, String username) {
        return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
