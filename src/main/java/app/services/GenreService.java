package app.services;

import app.DAO.GenreDAO;

import app.DTO.GenreResponseDTO;
import app.entities.Genre;

import app.exceptions.ApiException;

import java.util.List;
import java.util.stream.Collectors;

public class GenreService {

    private final GenreDAO genreDAO;

    public GenreService(GenreDAO genreDAO) {
        this.genreDAO = genreDAO;
    }

    public List<GenreResponseDTO> getAllGenres() {
        return genreDAO.getAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public GenreResponseDTO getGenreById(Integer id) {
        Genre genre = genreDAO.getById(id);
        if (genre == null) {
            throw new ApiException(404, "Genre with id " + id + " not found");
        }
        return toDTO(genre);
    }

    private GenreResponseDTO toDTO(Genre genre) {
        return new GenreResponseDTO(genre.getId(),genre.getName());

    }
}
