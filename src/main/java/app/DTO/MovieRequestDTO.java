package app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequestDTO {
    String title;
    LocalDate releaseDate;
    Double tmdbRating;
    Integer tmdbId;
}