package ru.yandex.practicum.filmorate.service;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final InMemoryFilmStorage inMemoryFilmStorage;
    private final InMemoryUserStorage inMemoryUserStorage;

    public Collection<Film> findAll() {
        return inMemoryFilmStorage.findAll();
    }

    public Film getById(Long id) {
        return inMemoryFilmStorage.getById(id);
    }

    public Film create(Film film) {
        return inMemoryFilmStorage.create(film);
    }

    public Film update(Film film) {
        return inMemoryFilmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = inMemoryFilmStorage.getById(filmId);

        if (film == null) {
            throw new NotFoundException("Фильм не найден");
        }

        if (inMemoryUserStorage.getById(userId) == null) {
            throw new NotFoundException("Неизвестный пользователь");
        }

        film.getLikes().add(userId);
        log.info("Пользователь c ID: {} поставил лайк фильму с ID: {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = inMemoryFilmStorage.getById(filmId);

        if (film == null) {
            throw new NotFoundException("Фильм не найден");
        }

        if (inMemoryUserStorage.getById(userId) == null) {
            throw new NotFoundException("Неизвестный пользователь");
        }

        film.getLikes().remove(userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public Collection<Film> getMostPopular(int count) {
        return inMemoryFilmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
