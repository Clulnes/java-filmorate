package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class})
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @Test
    public void testCreateAndFindUserById() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("Login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);
        Long generatedId = createdUser.getId();

        Optional<User> userOptional = Optional.ofNullable(userStorage.getById(generatedId));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", generatedId)
                );
    }

    @Test
    public void testUpdateUser() {
        User newUser = new User();
        newUser.setEmail("user@email.ru");
        newUser.setLogin("login");
        newUser.setName("name");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));
        User createdUser = userStorage.create(newUser);

        createdUser.setName("new name");
        userStorage.update(createdUser);

        User updatedUser = userStorage.getById(createdUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("new name");
    }

    @Test
    public void testAddAndRemoveFriend() {
        User user1 = userStorage.create(createUser("user1@mail.ru", "login1"));
        User user2 = userStorage.create(createUser("user2@mail.ru", "login2"));

        userStorage.addFriend(user1.getId(), user2.getId());

        Collection<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends.size()).isEqualTo(1);
        assertThat(friends.iterator().next().getId()).isEqualTo(user2.getId());

        userStorage.removeFriend(user1.getId(), user2.getId());
        assertThat(userStorage.getFriends(user1.getId()).isEmpty()).isTrue();
    }

    @Test
    public void testCommonFriends() {
        User user1 = userStorage.create(createUser("u1@mail.ru", "l1"));
        User user2 = userStorage.create(createUser("u2@mail.ru", "l2"));
        User common = userStorage.create(createUser("common@mail.ru", "common"));

        userStorage.addFriend(user1.getId(), common.getId());
        userStorage.addFriend(user2.getId(), common.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends.size()).isEqualTo(1);
        assertThat(commonFriends.iterator().next().getId()).isEqualTo(common.getId());
    }

    @Test
    public void testCreateAndGetFilm() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);

        Film savedFilm = filmStorage.getById(createdFilm.getId());

        assertThat(savedFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Test Film")
                .hasFieldOrPropertyWithValue("description", "Description");

        assertThat(savedFilm.getMpa().getId()).isEqualTo(1);
    }

    @Test
    public void testUpdateFilm() {
        Film film = filmStorage.create(createTestFilm());

        film.setName("Updated Name");
        filmStorage.update(film);

        Film updatedFilm = filmStorage.getById(film.getId());
        assertThat(updatedFilm.getName()).isEqualTo("Updated Name");
    }

    @Test
    public void testLikes() {
        Film film = filmStorage.create(createTestFilm());
        User user1 = userStorage.create(createUser("like1@mail.ru", "l1"));
        User user2 = userStorage.create(createUser("like2@mail.ru", "l2"));

        filmStorage.addLike(film.getId(), user1.getId());
        filmStorage.addLike(film.getId(), user2.getId());

        Collection<Film> popular = filmStorage.getMostPopular(10);
        assertThat(popular.iterator().next().getId()).isEqualTo(film.getId());
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login + "Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);
        film.setMpa(new Rating(1, "G"));
        return film;
    }
}
