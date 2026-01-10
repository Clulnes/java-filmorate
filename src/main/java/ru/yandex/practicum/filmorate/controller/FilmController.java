package ru.yandex.practicum.filmorate.controller;

import exception.ValidationException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Запрос на добавление фильма с названием: {}", film.getName());

        validate(film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм добавлен, его ID: {}", film.getId());
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации: выбранная дата слишком старая");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Запрос на обновление данных фильма с ID: {}", newFilm.getId());

        if (newFilm.getId() == null) {
            log.warn("Ошибка компиляции, не указан ID");
            throw new ValidationException("Id должен быть указан");
        }

        validate(newFilm);

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());

            log.info("Фильм с ID: {} успешно обновлен", newFilm.getId());
            return oldFilm;
        }

        log.warn("Ошибка обновления, в базе нет фильма с ID: {}", newFilm.getId());
        throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
    }
}
