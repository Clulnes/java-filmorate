package ru.yandex.practicum.filmorate.storage.dao;

import exception.NotFoundException;
import exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        validateMpaAndGenres(film);

        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
                ps.setLong(4, film.getDuration());

                if (film.getMpa() != null && film.getMpa().getId() != 0) {
                    ps.setInt(5, film.getMpa().getId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                film.setId(keyHolder.getKey().longValue());
            }

            saveGenres(film);
            return getById(film.getId());

        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка сохранения фильма");
        }
    }

    @Override
    public Film update(Film film) {
        getById(film.getId());
        validateMpaAndGenres(film);

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);

        return getById(film.getId());
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, r.name as mpa_name FROM films f LEFT JOIN ratings r ON f.rating_id = r.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        loadGenres(films);
        return films;
    }

    @Override
    public Film getById(Long id) {
        String sql = "SELECT f.*, r.name as mpa_name FROM films f LEFT JOIN ratings r ON f.rating_id = r.id WHERE f.id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
            loadGenres(Collections.singletonList(film));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM likes WHERE film_id = ? AND user_id = ?", Integer.class, filmId, userId);

        if (count != null && count == 0) {
            jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        int rows = jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);

        if (rows == 0) {
            throw new NotFoundException("Лайк не найден");
        }
    }

    @Override
    public Collection<Film> getMostPopular(int count) {
        String sql = "SELECT f.*, r.name as mpa_name, COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        loadGenres(films);
        return films;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        if (rs.getDate("release_date") != null) {
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        }
        film.setDuration(rs.getLong("duration"));

        if (rs.getObject("rating_id") != null) {
            film.setMpa(new Rating(rs.getInt("rating_id"), rs.getString("mpa_name")));
        }
        return film;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> uniqueGenreIds = film.getGenres().stream()
                    .filter(Objects::nonNull)
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            List<Object[]> batchArgs = new ArrayList<>();

            for (Integer genreId : uniqueGenreIds) {
                batchArgs.add(new Object[]{film.getId(), genreId});
            }
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void loadGenres(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        for (Film film : films) {
            film.getGenres().clear();
        }

        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        Object[] args = films.stream().map(Film::getId).toArray();

        String sql = "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name " +
                "FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ") " +
                "ORDER BY g.id";

        Map<Long, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, f -> f));

        jdbcTemplate.query(sql, new ResultSetExtractor<Void>() {

            @Override
            public Void extractData(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    Long filmId = rs.getLong("film_id");
                    Film film = filmById.get(filmId);

                    if (film != null) {
                        film.getGenres().add(new Genre(rs.getInt("genre_id"),
                                rs.getString("genre_name")));
                    }
                }
                return null;
            }
        }, args);
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != 0) {
            Integer mpaCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM ratings WHERE id = ?", Integer.class, film.getMpa().getId());

            if (mpaCount == null || mpaCount == 0) {
                throw new NotFoundException("MPA рейтинг не найден");
            }
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {

                if (genre == null || genre.getId() == 0) continue;
                Integer genreCount = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM genres WHERE id = ?", Integer.class, genre.getId());

                if (genreCount == null || genreCount == 0) {
                    throw new NotFoundException("Жанр не найден");
                }
            }
        }
    }
}