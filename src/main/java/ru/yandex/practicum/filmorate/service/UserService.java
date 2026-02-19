package ru.yandex.practicum.filmorate.service;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final InMemoryUserStorage inMemoryUserStorage;

    public Collection<User> findAll() {
        return inMemoryUserStorage.findAll();
    }

    public User create(User user) {
        return inMemoryUserStorage.create(user);
    }

    public User update(User user) {
        return inMemoryUserStorage.update(user);
    }

    public User getById(Long id) {
        return inMemoryUserStorage.getById(id);
    }

    public void addFriend(Long userId, Long friendId) {
        User user = inMemoryUserStorage.getById(userId);
        User friend = inMemoryUserStorage.getById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.info("Пользователь с ID {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = inMemoryUserStorage.getById(userId);
        User friend = inMemoryUserStorage.getById(friendId);

        if (user == null || friend == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        User user = inMemoryUserStorage.getById(userId);
        User friend = inMemoryUserStorage.getById(friendId);

        if (user == null || friend == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        return user.getFriends().stream()
                .filter(id -> friend.getFriends().contains(id))
                .map(inMemoryUserStorage::getById)
                .collect(Collectors.toList());
    }

    public Collection<User> getFriends(Long userId) {
        User user = inMemoryUserStorage.getById(userId);

        return user.getFriends().stream()
                .map(inMemoryUserStorage::getById)
                .collect(Collectors.toList());
    }
}
