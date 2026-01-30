package ru.yandex.practicum.filmorate.controller;

import exception.ValidationException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос на внесение нового пользователя в базу: {}", user.getLogin());

        user.setId(getNextId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя не внесено, заменено на логин: {}", user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлен, {} в базе с ID: {}", user.getName(), user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Получен запрос на обновление пользователя с ID: {}", newUser.getId());

        if (newUser.getId() == null) {
            log.warn("Ошибка валидации: не указан ID");
            throw new ValidationException("Id должен быть указан");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            if (newUser.getName() == null || newUser.getName().isBlank()) {
                oldUser.setName(newUser.getLogin());
            } else {
                oldUser.setName(newUser.getName());
            }
            oldUser.setBirthday(newUser.getBirthday());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            log.info("Данные пользователя с ID: {} успешно обновлены", newUser.getId());
            return oldUser;
        }

        log.warn("Ошибка обновления: пользователь с ID: {} не найден", newUser.getId());
        throw new ValidationException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
