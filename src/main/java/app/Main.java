package app;

import app.DAO.GenreDAO;
import app.config.HibernateConfig;
import app.controllers.GenreController;
import app.controllers.MovieController;
import app.DAO.MovieDAO;
import app.exceptions.ApiException;
import app.services.GenreService;
import app.services.MovieService;
import app.services.TmdbClient;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import jakarta.persistence.EntityManagerFactory;

import java.util.Map;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        MovieDAO movieDAO = new MovieDAO(emf);
        MovieService movieService = new MovieService(movieDAO);
        MovieController movieController = new MovieController(movieService);

        GenreDAO genreDAO = new GenreDAO(emf);
        GenreService genreService = new GenreService(genreDAO);
        GenreController genreController = new GenreController(genreService);


        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }));
        }).start(7070);


        //Movie routes

        app.get("/api/movies", movieController::getAll);
        app.get("/api/movies/{id}", movieController::getById);
        app.delete("/api/movies/{id}", movieController::delete);
        app.post("/api/movies", movieController::create);
        app.put("/api/movies/{id}", movieController::update);

        //Genre Routes
        app.get("/api/genres", genreController::getAll);
        app.get("/api/genres/{id}", genreController::getById);


        app.exception(ApiException.class, (e, ctx) -> {
            ctx.status(e.getCode());
            ctx.json(Map.of("status", e.getCode(), "msg", e.getMessage()));
        });
    }
}