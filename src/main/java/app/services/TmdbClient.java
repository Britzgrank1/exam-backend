package app.services;

import app.DTO.TmdbGenreDTO;
import app.DTO.TmdbMovieDTO;
import app.exceptions.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TmdbClient {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<TmdbMovieDTO> fetchMoviePage(int page) {
        String apiKey = System.getenv("API_KEY");
        String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey + "&page=" + page;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ApiException(502, "TMDB request failed with status " + response.statusCode());
            }

            JsonNode results = mapper.readTree(response.body()).get("results");
            return mapper.readerForListOf(TmdbMovieDTO.class).readValue(results);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(502, "Could not fetch movies from TMDB: " + e.getMessage());
        }
    }
    public List<TmdbGenreDTO> fetchGenres() {
        String apiKey = System.getenv("API_KEY");
        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + apiKey;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ApiException(502, "TMDB request failed with status " + response.statusCode());
            }

            JsonNode genres = mapper.readTree(response.body()).get("genres");
            return mapper.readerForListOf(TmdbGenreDTO.class).readValue(genres);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(502, "Could not fetch genres from TMDB: " + e.getMessage());
        }
    }
}