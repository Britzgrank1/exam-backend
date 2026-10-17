package app.controllers;

import app.exceptions.ApiException;
import app.services.GenreService;
import io.javalin.http.Context;

public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }
    public void getAll(Context ctx) {
        ctx.status(200).json(genreService.getAllGenres());
    }

    public void getById(Context ctx) {
        int id = getId(ctx);
        ctx.status(200).json(genreService.getGenreById(id));
    }

    private int getId(Context ctx) {
        try {
            return Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Id must be a number");
        }
    }
}
