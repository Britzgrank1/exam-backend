package app.services;

import app.DTO.MovieResponseDTO;
import app.DAO.MovieDAO;
import app.entities.Genre;
import app.entities.Movie;
import app.exceptions.ApiException;

import java.util.List;
import java.util.stream.Collectors;

public class MovieService {

    private final MovieDAO movieDAO;

    public MovieService(MovieDAO movieDAO) {
        this.movieDAO = movieDAO;
    }

    public List<MovieResponseDTO> getAllMovies() {
        return movieDAO.getAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public MovieResponseDTO getMovieById(Integer id) {
        Movie movie = movieDAO.getById(id);
        if (movie == null) {
            throw new ApiException(404, "Movie with id " + id + " not found");
        }
        return toDTO(movie);
    }

    private MovieResponseDTO toDTO(Movie movie) {
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
}