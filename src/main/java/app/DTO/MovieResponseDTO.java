package app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDTO {
    Integer id;
    String title;
    LocalDate releaseDate;
    Double tmdbRating;
    List<String> genres;
}