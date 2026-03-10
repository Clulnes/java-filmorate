package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, UserService.class})
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final UserService userService;

    @Test
    public void testFindUserById() {

        Optional<User> userOptional = Optional.ofNullable(userStorage.getById(1L));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testCreateAndGetById() {
        User newUser = new User();
        newUser.setEmail("user@email.ru");
        newUser.setLogin("login");
        newUser.setName("name");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(newUser);

        User savedUser = userStorage.getById(createdUser.getId());

        assertThat(savedUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", createdUser.getId())
                .hasFieldOrPropertyWithValue("email", "user@email.ru");
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
    public void testFindAllUsers() {
        User user1 = new User();
        user1.setEmail("1@mail.ru"); user1.setLogin("l1"); user1.setName("n1"); user1.setBirthday(LocalDate.now());
        User user2 = new User();
        user2.setEmail("2@mail.ru"); user2.setLogin("l2"); user2.setName("n2"); user2.setBirthday(LocalDate.now());

        userStorage.create(user1);
        userStorage.create(user2);

        Collection<User> users = userStorage.findAll();
        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    public void testAddAndRemoveFriend() {
        User user1 = new User();
        user1.setEmail("1@mail.ru");
        user1.setLogin("l1");
        user1.setName("n1");
        user1.setBirthday(LocalDate.now());
        User user2 = new User();
        user2.setEmail("2@mail.ru");
        user2.setLogin("l2");
        user2.setName("n2");
        user2.setBirthday(LocalDate.now());

        userService.addFriend(user1.getId(), user2.getId());

        Collection<User> friends = userService.getFriends(user1.getId());
        assertThat(friends.size()).isEqualTo(1);
        assertThat(friends.iterator().next().getId()).isEqualTo(user2.getId());

        userService.removeFriend(user1.getId(), user2.getId());
        assertThat(userService.getFriends(user1.getId())).isEqualTo(null);
    }

    @Test
    public void testCommonFriends() {
        User user1 = new User();
        user1.setEmail("1@mail.ru");
        user1.setLogin("l1");
        user1.setName("n1");
        user1.setBirthday(LocalDate.now());
        User user2 = new User();
        user2.setEmail("2@mail.ru");
        user2.setLogin("l2");
        user2.setName("n2");
        user2.setBirthday(LocalDate.now());
        User common = new User();
        common.setEmail("common@mail.ru");
        common.setLogin("common");
        common.setName("n2");
        common.setBirthday(LocalDate.now());

        userService.addFriend(user1.getId(), common.getId());
        userService.addFriend(user2.getId(), common.getId());

        Collection<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends.size()).isEqualTo(1);
        assertThat(commonFriends.iterator().next().getId()).isEqualTo(common.getId());
    }

    @Test
    public void testCreateAndGetFilm() {
        Film film = createTestFilm();

        Film savedFilm = filmStorage.getById(film.getId());

        assertThat(savedFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Test Film")
                .hasFieldOrPropertyWithValue("description", "Description");

        assertThat(savedFilm.getRating().getId()).isEqualTo(1);
        assertThat(savedFilm.getGenre().getId()).isEqualTo(1);
    }

    @Test
    public void testUpdateFilm() {
        Film film = createTestFilm();
        film.setName("Updated Name");
        filmStorage.update(film);

        Film updatedFilm = filmStorage.getById(film.getId());
        assertThat(updatedFilm.getName()).isEqualTo("Updated Name");
    }

    @Test
    public void testFindAllFilms() {
        createTestFilm();
        createTestFilm();

        Collection<Film> films = filmStorage.findAll();
        assertThat(films.size()).isEqualTo(2);
    }

    @Test
    public void testLikes() {
        Film film = createTestFilm();
        User user1 = new User();
        user1.setEmail("1@mail.ru");
        user1.setLogin("l1");
        user1.setName("n1");
        user1.setBirthday(LocalDate.now());
        User user2 = new User();
        user2.setEmail("2@mail.ru");
        user2.setLogin("l2");
        user2.setName("n2");
        user2.setBirthday(LocalDate.now());
        userStorage.create(user1);
        userStorage.create(user2);

        filmStorage.addLike(film.getId(), 1L);
        filmStorage.addLike(film.getId(), 2L);

        Collection<Film> popular = filmStorage.getMostPopular(10);
        assertThat(popular).isNotNull();
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Rating rating = new Rating(1, "G");
        film.setRating(rating);

        Genre genre = new Genre(1, "Комедия");
        film.setGenre(genre);

        return filmStorage.create(film);
    }
}
