package app;

import app.DAO.GenreDAO;
import app.DAO.SecurityDAO;
import app.config.HibernateConfig;
import app.controllers.GenreController;
import app.controllers.MovieController;
import app.DAO.MovieDAO;
import app.controllers.SecurityController;
import app.exceptions.ApiException;
import app.services.GenreService;
import app.services.ImportService;
import app.services.MovieService;
import app.services.TmdbClient;
import app.DAO.RatingDAO;
import app.services.RatingService;
import app.controllers.RatingController;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import jakarta.persistence.EntityManagerFactory;
import app.security.AccessRole;

import java.util.Map;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        startServer(7070, emf);
    }
    public static Javalin startServer(int port, EntityManagerFactory emf) {



        MovieDAO movieDAO = new MovieDAO(emf);
        MovieService movieService = new MovieService(movieDAO);
        MovieController movieController = new MovieController(movieService);

        GenreDAO genreDAO = new GenreDAO(emf);

        GenreService genreService = new GenreService(genreDAO);
        GenreController genreController = new GenreController(genreService);

        SecurityDAO securityDAO = new SecurityDAO(emf);
        SecurityController securityController = new SecurityController(securityDAO);

        RatingDAO ratingDAO = new RatingDAO(emf);
        RatingService ratingService = new RatingService(ratingDAO);
        RatingController ratingController = new RatingController(ratingService);

//        ImportService importService = new ImportService(new TmdbClient(), genreDAO, movieDAO);
//        importService.importGenres();
//        importService.importMovies(2);

        //Test til at se om jeg modtager genrene
//        new TmdbClient().fetchGenres().forEach(g -> System.out.println(g.getId() + " " + g.getName()));

        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }));
        }).start(port);

        app.beforeMatched(securityController::authenticate);
        app.beforeMatched(securityController::authorize);


        // Healthcheck bliver brugt til docker og Caddy
        app.get("/api/healthcheck", ctx -> ctx.json(Map.of("msg", "API is up and running")));

        // Auth/Security Routes
        app.post("/api/auth/register", securityController::register,AccessRole.ANYONE);
        app.post("/api/auth/login", securityController::login,AccessRole.ANYONE);

        //Movie routes

        app.get("/api/movies", movieController::getAll);
        app.get("/api/movies/{id}", movieController::getById);
        app.delete("/api/movies/{id}", movieController::delete,AccessRole.ADMIN);
        app.post("/api/movies", movieController::create,AccessRole.ADMIN);
        app.put("/api/movies/{id}", movieController::update,AccessRole.ADMIN);


        //Rating
        app.post("/api/movies/{id}/rating", ratingController::rateMovie, AccessRole.USER);
        app.get("/api/recommendations", ratingController::getRecommendation, AccessRole.USER);


        //Genre Routes
        app.get("/api/genres", genreController::getAll);
        app.get("/api/genres/{id}", genreController::getById);


        app.exception(ApiException.class, (e, ctx) -> {
            ctx.status(e.getCode());
            ctx.json(Map.of("status", e.getCode(), "msg", e.getMessage()));
        });
        return app;
    }
}