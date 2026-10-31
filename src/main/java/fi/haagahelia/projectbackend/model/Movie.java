package fi.haagahelia.projectbackend.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String director;
    private int releaseYear;

    @ManyToOne
    @JoinColumn(name = "genreid")
    private Genre genre;

    // Hidden from JSON so the API doesn't expose users (and their password hashes)
    @JsonIgnore
    @OneToMany(mappedBy = "movie", cascade = CascadeType.REMOVE)
    private List<Rating> ratings = new ArrayList<>();

    public Movie() {
    }

    public Movie(String title, String director, int releaseYear) {
        this.title = title;
        this.director = director;
        this.releaseYear = releaseYear;
    }

    // Average of everyone's ratings, or null if nobody has rated yet
    public Double getAverageRating() {
        if (ratings == null || ratings.isEmpty()) {
            return null;
        }
        return ratings.stream().mapToInt(Rating::getScore).average().getAsDouble();
    }

    public int getRatingCount() {
        return ratings == null ? 0 : ratings.size();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDirector() {
        return director;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public Genre getGenre() {
        return genre;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    @Override
    public String toString() {
        return "Movie id=" + id + ", title=" + title + ", director=" + director;
    }
}
