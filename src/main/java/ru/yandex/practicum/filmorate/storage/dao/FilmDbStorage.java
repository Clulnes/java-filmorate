package ru.yandex.practicum.filmorate.storage.dao;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
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
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id, genre_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setInt(5, film.getRating().getId());
            ps.setInt(6, film.getGenre().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ?," +
                " genre_id = ? WHERE id = ?";
        int updatedRows = jdbcTemplate.update(sql,
                film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getRating().getId(), film.getGenre().getId(), film.getId());

        if (updatedRows == 0) throw new NotFoundException("Фильм не найден");
        return getById(film.getId());
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, r.name as rating_name, g.name as genre_name " +
                "FROM films as f " +
                "JOIN ratings as r ON f.rating_id = r.id " +
                "JOIN genres as g ON f.genre_id = g.id";

        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film getById(Long id) {
        String sql = "SELECT f.*, r.name as rating_name, g.name as genre_name " +
                "FROM films as f " +
                "JOIN ratings as r ON f.rating_id = r.id " +
                "JOIN genres as g ON f.genre_id = g.id " +
                "WHERE f.id = ?";

        return jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String checkSql = "SELECT count(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count != null && count > 0) {
            return;
        }

        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
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

        return jdbcTemplate.query(sql, this::mapRowToFilm, count);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));
        film.setRating(new Rating(rs.getInt("rating_id"), rs.getString("rating_name")));
        film.setGenre(new Genre(rs.getInt("genre_id"), rs.getString("genre_name")));

        return film;
    }
}
