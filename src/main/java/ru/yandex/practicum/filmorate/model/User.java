package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    Long id;

    @Email(message = "Email должен иметь символ @ иметь стандартный вид")
    @NotEmpty(message = "Email не может быть пустым")
    String email;

    @NotEmpty(message = "Login не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    String login;
    String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;
}
