package app.persistance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class TmdbService {

    public MovieDTO getMovieById(int id) {

        String apiKey = System.getenv("API_KEY");

        String url = "https://api.themoviedb.org/3/movie/" + id + "?api_key="
                + apiKey;

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                MovieDTO movie =
                        objectMapper.readValue(response.body(), MovieDTO.class);

                return movie;

            } else {
                System.out.println("Request failed. Status code: "
                        + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public List<MovieDTO> getSortedByReleaseDate(String query) {

        String apiKey = System.getenv("API_KEY");

        String url = "https://api.themoviedb.org/3/search/movie?api_key="
                + apiKey
                + "&query=" + query;

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                JsonNode json = objectMapper.readTree(response.body());

                JsonNode results = json.get("results");

                List<MovieDTO> movies = objectMapper
                        .readerForListOf(MovieDTO.class)
                        .readValue(results);

                return movies.stream()
                        .filter(movie -> movie.getReleaseDate() != null)
                        .sorted((movie1, movie2) ->
                                movie2.getReleaseDate()
                                        .compareTo(movie1.getReleaseDate()))
                        .toList();

            } else {
                System.out.println("Request failed. Status code: "
                        + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<MovieDTO> getByRating(double minRating, double maxRating) {

        String apiKey = System.getenv("API_KEY");

        List<MovieDTO> allMovies = new ArrayList<>();

        try {
            HttpClient client = HttpClient.newHttpClient();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            int page = 1;
            int totalPages = 1;

            do {
                String url = "https://api.themoviedb.org/3/discover/movie?api_key="
                        + apiKey
                        + "&vote_average.gte=" + minRating
                        + "&vote_average.lte=" + maxRating
                        + "&page=" + page;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .GET()
                        .build();

                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {

                    JsonNode json = objectMapper.readTree(response.body());
                    totalPages = json.get("total_pages").asInt();

                    JsonNode results = json.get("results");

                    List<MovieDTO> pageMovies = objectMapper
                            .readerForListOf(MovieDTO.class)
                            .readValue(results);

                    allMovies.addAll(pageMovies);

                } else {
                    System.out.println("Request failed. Status code: "
                            + response.statusCode());
                    break;
                }

                page++;

            } while (page <= totalPages);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return allMovies.stream()
                .filter(movie -> movie.getRating() >= minRating
                        && movie.getRating() <= maxRating)
                .toList();
    }
}
