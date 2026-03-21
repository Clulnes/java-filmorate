package ru.yandex.practicum.filmorate.storage.dao;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class RatingDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Collection<Rating> findAll() {
        String sql = "SELECT * FROM ratings ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    public Rating getById(int id) {
        String sql = "SELECT * FROM ratings WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToMpa, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг МРА с id = " + id + " не найден");
        }
    }

    private Rating mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Rating(
                rs.getInt("id"),
                rs.getString("name")
        );
    }
}
