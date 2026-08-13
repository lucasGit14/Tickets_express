package br.com.ticketsexpress.tickets_express_api.integration;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@Service
public class TmdbService {

    @Value("${tmdb.api.key:}")
    private String apiKey;

    @Value("${tmdb.api.url:https://api.themoviedb.org/3}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getMovieDetails(Long tmdbMovieId) {
        if (apiKey == null || apiKey.isEmpty()) {
            return getStubMovieDetails(tmdbMovieId);
        }

        try {
            String url = String.format("%s/movie/%d?api_key=%s&language=pt-BR", apiUrl, tmdbMovieId, apiKey);
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            return getStubMovieDetails(tmdbMovieId);
        }
    }

    public List<Map<String, Object>> searchMovies(String query) {
        if (apiKey == null || apiKey.isEmpty()) {
            return getStubSearchResults(query);
        }

        try {
            String url = String.format("%s/search/movie?api_key=%s&language=pt-BR&query=%s", apiUrl, apiKey, query);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return (List<Map<String, Object>>) response.get("results");
        } catch (Exception e) {
            return getStubSearchResults(query);
        }
    }

    private Map<String, Object> getStubMovieDetails(Long tmdbMovieId) {
        return Map.of(
            "id", tmdbMovieId,
            "title", "Filme Exemplo " + tmdbMovieId,
            "overview", "Sinopse de exemplo para o filme. Em produção, isso será substituído por dados reais da API TMDb.",
            "poster_path", "/example_poster.jpg",
            "release_date", "2024-12-01"
        );
    }

    private List<Map<String, Object>> getStubSearchResults(String query) {
        return List.of(
            Map.of(
                "id", 12345,
                "title", "Show de Rock Festival",
                "overview", "Um espetáculo incrível de rock com as melhores bandas do país.",
                "poster_path", "/rock_poster.jpg",
                "release_date", "2024-12-01"
            ),
            Map.of(
                "id", 67890,
                "title", "Concerto de Jazz",
                "overview", "Noite especial com músicos de jazz renomados.",
                "poster_path", "/jazz_poster.jpg",
                "release_date", "2024-12-15"
            )
        );
    }
}
