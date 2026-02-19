package ru.yandex.practicum.filmorate.storage.film;

import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм создан: {}", film.getName());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Фильм не найден");
        }

        newFilm.setLikes(oldFilm.getLikes());

        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public Film getById(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        return films.get(id);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
