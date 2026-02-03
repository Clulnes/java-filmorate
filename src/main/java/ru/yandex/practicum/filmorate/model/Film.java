package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {
    Long id;

    @NotEmpty(message = "Название не может быть пустым")
    String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    String description;

    @ReleaseDate
    LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    Long duration;

    Set<Long> likes = new HashSet<>();
}
