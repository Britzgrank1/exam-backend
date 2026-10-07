package app.persistance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieDTO {
    @Setter
    @Getter
    private String title;

    @Setter
    @Getter
    private String overview;

    @Getter
    @Setter
    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @Getter
    @Setter
    @JsonProperty("vote_average")
    private double rating;

    public String getReleaseYear() {
        return String.valueOf(releaseDate.getYear());
    }

}