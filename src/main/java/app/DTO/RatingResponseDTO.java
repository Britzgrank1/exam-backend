package app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class RatingResponseDTO {
    Integer id;
    Integer movieId;
    String movieTitle;
    String username;
    Integer rating;
    String comment;
}
