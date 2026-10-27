package app.services;

import app.DAO.MovieDAO;
import app.DAO.RatingDAO;
import app.DTO.*;
import app.entities.Genre;
import app.entities.Movie;
import app.entities.User;
import app.entities.UserMovieRating;
import app.exceptions.ApiException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RatingService {

    private final RatingDAO ratingDAO;

    public RatingService(RatingDAO ratingDAO) {
        this.ratingDAO = ratingDAO;
    }

    public RatingResponseDTO rateMovie(String username, Integer movieId, RateMovieDTO dto) {
        validate(dto);
        UserMovieRating saved = ratingDAO.rateMovie(username,movieId,dto.getRating(),dto.getComment());
        return toDTO(saved);
        }

    public RecommendationDTO getRecommendations(String username) {

        // Kigger på de film brugeren har rated 7 eller over
        List<UserMovieRating> liked = ratingDAO.getLikedRatings(username, 7);

        List<String> becauseYouLiked = liked.stream()
                .map(r -> r.getMovie().getTitle())
                .collect(Collectors.toList());

        //Samler alle genrerne fra de film i et set
        Set<Integer> genreIds = liked.stream()
                .flatMap(r -> r.getMovie().getGenres().stream())
                .map(Genre::getId)
                .collect(Collectors.toSet());
        //Hvis bruger ikke har givet en høj rating så bliver der ikke anbefalet
        if (genreIds.isEmpty()){
            return new RecommendationDTO(becauseYouLiked, List.of());
        }
        //Kigger på listen af film og tager de 5 bedste som sendes tilbage og fremvises til user
        List<MovieResponseDTO> recommendations = ratingDAO.getMoviesInGenresNotRatedBy(username, genreIds)
                .stream()
                .limit(5)
                .map(this::toMovieDTO)
                .collect(Collectors.toList());

                return new RecommendationDTO(becauseYouLiked, recommendations);
    }
    private MovieResponseDTO toMovieDTO(Movie movie){
        List<String> genreNames = movie.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toList());

        return new MovieResponseDTO(
                movie.getId(),
                movie.getTitle(),
                movie.getReleaseDate(),
                movie.getTmdbRating(),
                genreNames
        );
    }


    private RatingResponseDTO toDTO(UserMovieRating rating) {
        return new RatingResponseDTO(
                rating.getId(),
                rating.getMovie().getId(),
                rating.getMovie().getTitle(),
                rating.getUsers().getUsername(),
                rating.getRating(),
                rating.getComment()

        );
    }


    private void validate(RateMovieDTO dto) {
        if (dto.getRating() == null || dto.getRating() <1 || dto.getRating() > 10) {
            throw new ApiException(400, "Rating must be between 1-10");
        }
    }
}
