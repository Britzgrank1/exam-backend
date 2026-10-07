package app;

import app.config.HibernateConfig;
import app.controllers.MovieController;
import app.DAO.MovieDAO;
import app.exceptions.ApiException;
import app.services.MovieService;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;

import java.util.Map;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        MovieDAO movieDAO = new MovieDAO(emf);
        MovieService movieService = new MovieService(movieDAO);
        MovieController movieController = new MovieController(movieService);

        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(7070);

        app.get("/api/movies", movieController::getAll);
        app.get("/api/movies/{id}", movieController::getById);

        app.exception(ApiException.class, (e, ctx) -> {
            ctx.status(e.getCode());
            ctx.json(Map.of("status", e.getCode(), "msg", e.getMessage()));
        });
    }
}