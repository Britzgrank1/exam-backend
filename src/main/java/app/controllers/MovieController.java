package app.controllers;

import app.DTO.MovieRequestDTO;
import app.exceptions.ApiException;
import app.services.MovieService;
import io.javalin.http.Context;

public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    public void getAll(Context ctx) {
        ctx.status(200).json(movieService.getAllMovies());
    }

    public void getById(Context ctx) {
        int id = getId(ctx);
        ctx.status(200).json(movieService.getMovieById(id));
    }

    public void create(Context ctx) {
        MovieRequestDTO dto = ctx.bodyAsClass(MovieRequestDTO.class);
        ctx.status(201).json(movieService.createMovie(dto));
    }

    public void update(Context ctx) {
        int id = getId(ctx);
        MovieRequestDTO dto = ctx.bodyAsClass(MovieRequestDTO.class);
        ctx.status(200).json(movieService.updateMovie(id, dto));
    }

    public void delete(Context ctx) {
        int id = getId(ctx);
        movieService.deleteMovie(id);
        ctx.status(204);
    }

    private int getId(Context ctx) {
        try {
            return Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Id must be a number");
        }
    }
}