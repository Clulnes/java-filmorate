package ru.yandex.practicum.filmorate;

import exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {

    private UserService userService;
    private FilmService filmService;
    private InMemoryFilmStorage inMemoryFilmStorage;
    private InMemoryUserStorage inMemoryUserStorage;

    @BeforeEach
    public void setUp() {
        inMemoryUserStorage = new InMemoryUserStorage();
        inMemoryFilmStorage = new InMemoryFilmStorage();
        userService = new UserService(inMemoryUserStorage);
        filmService = new FilmService(inMemoryFilmStorage, inMemoryUserStorage);
    }

    @Test
    public void shouldCreateUserWithCorrectData() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("email@mail.ru");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = inMemoryUserStorage.create(user);

        assertNotNull(createdUser.getId());
        assertEquals("login", createdUser.getLogin());
        assertEquals("email@mail.ru", createdUser.getEmail());
        assertEquals("name", createdUser.getName());
        assertEquals(LocalDate.of(2000, 1, 1), createdUser.getBirthday());
        assertEquals(1, inMemoryUserStorage.findAll().size());
    }

    @Test
    public void shouldUpdateUser() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("email@mail.ru");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = inMemoryUserStorage.create(user);

        assertEquals("login", createdUser.getLogin());
        assertEquals("email@mail.ru", createdUser.getEmail());
        assertEquals("name", createdUser.getName());
        assertEquals(LocalDate.of(2000, 1, 1), createdUser.getBirthday());
        assertEquals(1, inMemoryUserStorage.findAll().size());

        User toUpdate = new User();
        toUpdate.setId(createdUser.getId());
        toUpdate.setLogin("newlogin");
        toUpdate.setEmail("newemail@mail.ru");
        toUpdate.setName("newname");
        toUpdate.setBirthday(LocalDate.of(2001, 1, 1));

        User updatedUser = inMemoryUserStorage.update(toUpdate);

        assertEquals("newlogin", updatedUser.getLogin());
        assertEquals("newemail@mail.ru", updatedUser.getEmail());
        assertEquals("newname", updatedUser.getName());
        assertEquals(LocalDate.of(2001, 1, 1), updatedUser.getBirthday());
        assertEquals(1, inMemoryUserStorage.findAll().size());
    }

    @Test
    public void shouldUseLoginAsNameIfNameIsEmpty() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("email@mail.ru");
        user.setName("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = inMemoryUserStorage.create(user);

        assertEquals("login", createdUser.getName());
    }

    @Test
    public void shouldThrowExceptionWhenUpdatingUnknownUser() {
        User user = new User();
        user.setId(999999L);
        user.setLogin("login");
        user.setEmail("email@mail.ru");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(NotFoundException.class, () -> inMemoryUserStorage.update(user));
    }

    @Test
    public void shouldCreateFilmWithCorrectData() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Film createdFilm = inMemoryFilmStorage.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals("name", createdFilm.getName());
        assertEquals("Description", createdFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
        assertEquals(120L, createdFilm.getDuration());
        assertEquals(1, inMemoryFilmStorage.findAll().size());
    }

    @Test
    public void shouldCreateFilmWithDescriptionThatHas200Chars() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Film createdFilm = inMemoryFilmStorage.create(film);

        assertNotNull(createdFilm);
    }

    @Test
    public void shouldCreateFilmWhenDateReleaseIs28December1895() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120L);

        Film createdFilm = inMemoryFilmStorage.create(film);

        assertNotNull(createdFilm);
    }

    @Test
    public void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Film createdFilm = inMemoryFilmStorage.create(film);

        assertEquals("name", createdFilm.getName());
        assertEquals("Description", createdFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
        assertEquals(120L, createdFilm.getDuration());
        assertEquals(1, inMemoryFilmStorage.findAll().size());

        Film toUpdate = new Film();
        toUpdate.setId(createdFilm.getId());
        toUpdate.setName("newname");
        toUpdate.setDescription("newDescription");
        toUpdate.setReleaseDate(LocalDate.of(2001, 1, 1));
        toUpdate.setDuration(150L);

        Film updatedFilm = inMemoryFilmStorage.update(toUpdate);

        assertEquals("newname", updatedFilm.getName());
        assertEquals("newDescription", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2001, 1, 1), updatedFilm.getReleaseDate());
        assertEquals(150L, updatedFilm.getDuration());
        assertEquals(1, inMemoryFilmStorage.findAll().size());
    }

    @Test
    public void shouldAddFriend() {
        User user1 = new User();
        user1.setLogin("login");
        user1.setEmail("email@mail.ru");
        user1.setName("");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setLogin("login");
        user2.setEmail("email@mail.ru");
        user2.setName("A");
        user2.setBirthday(LocalDate.of(2001, 1, 1));

        inMemoryUserStorage.create(user1);
        inMemoryUserStorage.create(user2);

        userService.addFriend(user1.getId(), user2.getId());

        assertTrue(user1.getFriends().contains(user2.getId()));
        assertTrue(user2.getFriends().contains(user1.getId()));
    }

    @Test
    public void shouldRemoveFriend() {
        User user1 = new User();
        user1.setLogin("login");
        user1.setEmail("email@mail.ru");
        user1.setName("");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setLogin("login");
        user2.setEmail("email@mail.ru");
        user2.setName("A");
        user2.setBirthday(LocalDate.of(2001, 1, 1));

        inMemoryUserStorage.create(user1);
        inMemoryUserStorage.create(user2);

        userService.addFriend(user1.getId(), user2.getId());
        userService.removeFriend(user1.getId(), user2.getId());

        assertEquals(0, user1.getFriends().size());
        assertEquals(0, user2.getFriends().size());
    }

    @Test
    public void shouldReturnListOfFriends() {
        User user1 = new User();
        user1.setLogin("login");
        user1.setEmail("email@mail.ru");
        user1.setName("");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setLogin("login");
        user2.setEmail("email@mail.ru");
        user2.setName("A");
        user2.setBirthday(LocalDate.of(2001, 1, 1));

        User user3 = new User();
        user3.setLogin("login");
        user3.setEmail("email@mail.ru");
        user3.setName("AB");
        user3.setBirthday(LocalDate.of(2002, 1, 1));

        inMemoryUserStorage.create(user1);
        inMemoryUserStorage.create(user2);
        inMemoryUserStorage.create(user3);

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        assertEquals(2, user1.getFriends().size());
        assertTrue(user1.getFriends().contains(user2.getId()));
        assertTrue(user1.getFriends().contains(user3.getId()));
    }

    @Test
    public void shouldReturnListOfCommonFriends() {
        User user1 = new User();
        user1.setLogin("login");
        user1.setEmail("email@mail.ru");
        user1.setName("");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setLogin("login");
        user2.setEmail("email@mail.ru");
        user2.setName("A");
        user2.setBirthday(LocalDate.of(2001, 1, 1));

        User user3 = new User();
        user3.setLogin("login");
        user3.setEmail("email@mail.ru");
        user3.setName("AB");
        user3.setBirthday(LocalDate.of(2002, 1, 1));

        inMemoryUserStorage.create(user1);
        inMemoryUserStorage.create(user2);
        inMemoryUserStorage.create(user3);

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        assertEquals(1, userService.getCommonFriends(user2.getId(), user3.getId()).size());
        assertTrue(userService.getCommonFriends(user2.getId(), user3.getId()).contains(user1));
    }

    @Test
    public void shouldLikeFilmByUser() {
        User user1 = new User();
        user1.setLogin("login");
        user1.setEmail("email@mail.ru");
        user1.setName("");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        Film film = new Film();
        film.setName("name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        inMemoryFilmStorage.create(film);
        inMemoryUserStorage.create(user1);

        filmService.addLike(user1.getId(), film.getId());

        assertTrue(film.getLikes().contains(user1.getId()));
    }

    @Test
    public void shouldRemoveLikeByUser() {
        User user1 = new User();
        user1.setLogin("login");
        user1.setEmail("email@mail.ru");
        user1.setName("");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        Film film = new Film();
        film.setName("name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        inMemoryFilmStorage.create(film);
        inMemoryUserStorage.create(user1);

        filmService.addLike(user1.getId(), film.getId());
        filmService.removeLike(user1.getId(), film.getId());

        assertFalse(film.getLikes().contains(user1.getId()));
    }
}
