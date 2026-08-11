package br.com.ticketsexpress.tickets_express_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String jwtSecret,
        String qrSecret,
        Tmdb tmdb,
        String corsOrigin
) {
    public record Tmdb(String baseUrl, String token) {
    }
}
