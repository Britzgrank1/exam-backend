package app.services;

import app.DTO.MovieResponseDTO;
import app.DAO.MovieDAO;
import app.DTO.MovieRequestDTO;
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
    public MovieResponseDTO createMovie(MovieRequestDTO dto) {
        validate(dto);
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setTmdbRating(dto.getTmdbRating());
        movie.setTmdbId(dto.getTmdbId());
        movieDAO.create(movie);
        return toDTO(movie);
    }

    public MovieResponseDTO updateMovie(Integer id, MovieRequestDTO dto) {
        validate(dto);
        Movie movie = movieDAO.getById(id);
        if (movie == null) {
            throw new ApiException(404, "Movie with id " + id + " not found");
        }
        movie.setTitle(dto.getTitle());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setTmdbRating(dto.getTmdbRating());
        movie.setTmdbId(dto.getTmdbId());
        movieDAO.update(movie);
        return toDTO(movie);
    }

    public void deleteMovie(Integer id) {
        if (movieDAO.getById(id) == null) {
            throw new ApiException(404, "Movie with id " + id + " not found");
        }
        movieDAO.delete(id);
    }

    private void validate(MovieRequestDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new ApiException(400, "Field 'title' is required");
        }
    }
}