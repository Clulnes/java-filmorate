package ru.yandex.practicum.filmorate.storage.dao;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.util.Collection;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setLong(4, film.getDuration());

                if (film.getMpa() != null) {
                    ps.setInt(5, film.getMpa().getId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                return ps;
            }, keyHolder);
            film.setId(keyHolder.getKey().longValue());
            saveGenres(film);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Рейтинг или жанр не найден в базе данных");
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        getById(film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                " WHERE id = ?";

        try {
            jdbcTemplate.update(sql,
                    film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                    film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

            jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
            saveGenres(film);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Рейтинг или жанр не найден");
        }

        return getById(film.getId());
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, r.name as rating_name " +
                "FROM films as f " +
                "LEFT JOIN ratings as r ON f.rating_id = r.id";

        Collection<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        for (Film film : films) {
            loadGenres(film);
        }
        return films;
    }

    @Override
    public Film getById(Long id) {
        String sql = "SELECT f.*, r.name as rating_name " +
                "FROM films as f " +
                "LEFT JOIN ratings as r ON f.rating_id = r.id " +
                "WHERE f.id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
            loadGenres(film);
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "MERGE INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, filmId, userId);

        if (rowsAffected == 0) {
            throw new NotFoundException("Лайк от пользователя " + userId + " не найден");
        }
    }

    @Override
    public Collection<Film> getMostPopular(int count) {
        String sql = "SELECT f.*, r.name as rating_name, COUNT(l.user_id) AS likes_count " +
                "FROM films as f " +
                "LEFT JOIN ratings as r ON f.rating_id = r.id " +
                "LEFT JOIN likes as l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        Collection<Film> popularFilms = jdbcTemplate.query(sql, this::mapRowToFilm, count);

        for (Film film : popularFilms) {
            loadGenres(film);
        }
        return popularFilms;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));

        int ratingId = rs.getInt("rating_id");
        if (!rs.wasNull()) {
            film.setMpa(new Rating(ratingId, rs.getString("rating_name")));
        }
        return film;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "MERGE INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sql , film.getId(), genre.getId());
            }
        }
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.id, g.name FROM genres as g " +
                "JOIN film_genres as fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";

        Collection<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), film.getId()
        );
        film.getGenres().clear();
        film.getGenres().addAll(genres);
    }
}
