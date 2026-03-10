package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User getById(Long id) {
        return userStorage.getById(id);
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        userStorage.addFriend(userId, friendId);

        log.info("Пользователь с ID {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил пользователя {}", userId, friendId);
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        return userStorage.getCommonFriends(userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        userStorage.getById(userId);
        return userStorage.getFriends(userId);
    }
}
