package app.DAO;

import app.config.HibernateTestConfig;
import app.entities.Movie;
import app.testutils.MovieTestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MovieDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private MovieDAO movieDAO;
    private Map<String, Movie> seeded;

    @BeforeEach
    void beforeEach() {
        seeded = MovieTestPopulator.populate(emf);
        movieDAO = new MovieDAO(emf);
    }

    @Test
    void getAll() {
        List<Movie> all = movieDAO.getAll();

        assertThat(all, hasSize(3));
        assertThat(all.stream().map(Movie::getTitle).toList(),
                containsInAnyOrder("Test Movie 1", "Test Movie 2", "Test Movie 3"));
    }

    @Test
    void getById() {
        Movie seed = seeded.get("movie2");

        Movie fetched = movieDAO.getById(seed.getId());

        assertThat(fetched, notNullValue());
        assertThat(fetched.getTitle(), is("Test Movie 2"));
        assertThat(fetched.getGenres(), hasSize(2));   // virker kun fordi getById bruger JOIN FETCH
    }

    @Test
    void getById_withMissingId_returnsNull() {
        Movie fetched = movieDAO.getById(999_999);

        assertThat(fetched, nullValue());
    }

    @Test
    void create() {
        Movie movie = new Movie();
        movie.setTitle("New Test Movie");
        movie.setReleaseDate(LocalDate.of(2024, 5, 5));
        movie.setTmdbRating(9.0);
        movie.setTmdbId(2000);

        Movie created = movieDAO.create(movie);

        assertThat(created.getId(), notNullValue());
        assertThat(movieDAO.getAll(), hasSize(4));
        assertThat(movieDAO.getById(created.getId()).getTitle(), is("New Test Movie"));
    }

    @Test
    void update() {
        Movie seed = seeded.get("movie3");
        seed.setTitle("Updated Title");

        movieDAO.update(seed);

        assertThat(movieDAO.getById(seed.getId()).getTitle(), is("Updated Title"));
    }

    @Test
    void delete() {
        Movie seed = seeded.get("movie1");

        movieDAO.delete(seed.getId());

        assertThat(movieDAO.getById(seed.getId()), nullValue());
        assertThat(movieDAO.getAll(), hasSize(2));
    }

    @Test
    void getByTmdbId() {
        Movie fetched = movieDAO.getByTmdbId(1002);

        assertThat(fetched, notNullValue());
        assertThat(fetched.getTitle(), is("Test Movie 2"));
    }
}