package fi.haagahelia.projectbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// One user's rating of one movie. A user can rate each movie only once.
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "movie_id", "user_id" }))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private int score; // 1-5

    public Rating() {
    }

    public Rating(Movie movie, AppUser user, int score) {
        this.movie = movie;
        this.user = user;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
