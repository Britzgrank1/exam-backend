package app.controllers;

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
        int id = Integer.parseInt(ctx.pathParam("id"));
        ctx.status(200).json(movieService.getMovieById(id));
    }
}