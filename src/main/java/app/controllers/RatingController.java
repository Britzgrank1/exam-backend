package app.controllers;

import app.DTO.RateMovieDTO;
import app.DTO.UserDTO;
import app.exceptions.ApiException;
import app.services.RatingService;
import io.javalin.http.Context;

public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService){
        this.ratingService = ratingService;

    }

    public void rateMovie(Context ctx){
        int movieId = getId(ctx);
        UserDTO user = ctx.attribute("user");
        RateMovieDTO dto = ctx.bodyAsClass(RateMovieDTO.class);
        ctx.status(200).json(ratingService.rateMovie(user.username(), movieId,dto));

    }
    public void getRecommendation(Context ctx){
        UserDTO user = ctx.attribute("user");
        ctx.status(200).json(ratingService.getRecommendations(user.getUsername()));

    }
    private int getId(Context ctx) {
        try {
            return Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Id must be a number");
        }
    }
}
