package app.services;

import app.DAO.GenreDAO;
import app.DAO.MovieDAO;
import app.DTO.TmdbGenreDTO;
import app.DTO.TmdbMovieDTO;
import app.entities.Genre;
import app.entities.Movie;

import java.time.LocalDate;
import java.util.List;

public class ImportService {

    private final TmdbClient tmdbClient;
    private final GenreDAO genreDAO;
    private final MovieDAO movieDAO;

    public ImportService(TmdbClient tmdbClient, GenreDAO genreDAO, MovieDAO movieDAO) {
        this.tmdbClient = tmdbClient;
        this.genreDAO = genreDAO;
        this.movieDAO = movieDAO;
    }

    public void importGenres() {

        List<TmdbGenreDTO> genres = tmdbClient.fetchGenres();
        for (TmdbGenreDTO dto : genres) {
            Genre result = genreDAO.getByTmdbId(dto.getId());
            if (result == null) {
                Genre genre = new Genre();
                genre.setName(dto.getName());
                genre.setTmdbId(dto.getId());
                genreDAO.create(genre);
            }

        }
    }

    public void importMovies(int pages) {
        for (int page = 1; page <= pages; page++) {
            List<TmdbMovieDTO> dtos = tmdbClient.fetchMoviePage(page);

            for (TmdbMovieDTO dto : dtos) {
                if (movieDAO.getByTmdbId(dto.getId()) != null) {
                    continue;
                }

                Movie movie = new Movie();
                movie.setTitle(dto.getTitle());
                movie.setTmdbId(dto.getId());
                movie.setTmdbRating(dto.getTmdbRating());

                String date = dto.getReleaseDate();
                if (date != null && !date.isBlank()) {
                    movie.setReleaseDate(LocalDate.parse(date));
                }

                if (dto.getGenreIds() != null) {
                    for (Integer genreTmdbId : dto.getGenreIds()) {
                        Genre genre = genreDAO.getByTmdbId(genreTmdbId);
                        if (genre != null) {
                            movie.getGenres().add(genre);
                        }
                    }
                }

                movieDAO.create(movie);


            }
        }
    }
}
