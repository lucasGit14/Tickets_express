package br.com.ticketsexpress.tickets_express_api.event;

import br.com.ticketsexpress.tickets_express_api.auth.ApplicationUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private ApplicationUser organizer;

    @Column(name = "tmdb_movie_id", nullable = false)
    private Long tmdbMovieId;

    @Column(nullable = false)
    private String title;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(columnDefinition = "text")
    private String synopsis;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(nullable = false, length = 160)
    private String venue;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Event() {
    }

    public Event(UUID id, ApplicationUser organizer, Long tmdbMovieId, String title, String posterUrl, String synopsis, Instant startsAt, String venue, String address, BigDecimal price, EventStatus status, Instant createdAt) {
        this.id = id;
        this.organizer = organizer;
        this.tmdbMovieId = tmdbMovieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.synopsis = synopsis;
        this.startsAt = startsAt;
        this.venue = venue;
        this.address = address;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public ApplicationUser getOrganizer() {
        return organizer;
    }

    public Long getTmdbMovieId() {
        return tmdbMovieId;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public String getVenue() {
        return venue;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public EventStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
