package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

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

    LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    Duration duration;
}
