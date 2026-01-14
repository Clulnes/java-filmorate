package ru.yandex.practicum.filmorate;

import exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {

    private UserController userController;
    private FilmController filmController;

    @BeforeEach
    public void setUp() {
        userController = new UserController();
        filmController = new FilmController();
    }

    @Test
    public void shouldCreateUserWithCorrectData() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("email@mail.ru");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
        assertEquals("login", createdUser.getLogin());
        assertEquals("email@mail.ru", createdUser.getEmail());
        assertEquals("name", createdUser.getName());
        assertEquals(LocalDate.of(2000, 1, 1), createdUser.getBirthday());
        assertEquals(1, userController.findAll().size());
    }

    @Test
    public void shouldUpdateUser() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("email@mail.ru");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.create(user);

        assertEquals("login", createdUser.getLogin());
        assertEquals("email@mail.ru", createdUser.getEmail());
        assertEquals("name", createdUser.getName());
        assertEquals(LocalDate.of(2000, 1, 1), createdUser.getBirthday());
        assertEquals(1, userController.findAll().size());

        User toUpdate = new User();
        toUpdate.setId(createdUser.getId());
        toUpdate.setLogin("newlogin");
        toUpdate.setEmail("newemail@mail.ru");
        toUpdate.setName("newname");
        toUpdate.setBirthday(LocalDate.of(2001, 1, 1));

        User updatedUser = userController.update(toUpdate);

        assertEquals("newlogin", updatedUser.getLogin());
        assertEquals("newemail@mail.ru", updatedUser.getEmail());
        assertEquals("newname", updatedUser.getName());
        assertEquals(LocalDate.of(2001, 1, 1), updatedUser.getBirthday());
        assertEquals(1, userController.findAll().size());
    }

    @Test
    public void shouldUseLoginAsNameIfNameIsEmpty() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("email@mail.ru");
        user.setName("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.create(user);

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

        assertThrows(ValidationException.class, () -> userController.update(user));
    }

    @Test
    public void shouldCreateFilmWithCorrectData() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals("name", createdFilm.getName());
        assertEquals("Description", createdFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
        assertEquals(120L, createdFilm.getDuration());
        assertEquals(1, filmController.findAll().size());
    }

    @Test
    public void shouldCreateFilmWithDescriptionThatHas200Chars() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm);
    }

    @Test
    public void shouldCreateFilmWhenDateReleaseIs28December1895() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120L);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm);
    }

    @Test
    public void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);

        Film createdFilm = filmController.create(film);

        assertEquals("name", createdFilm.getName());
        assertEquals("Description", createdFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
        assertEquals(120L, createdFilm.getDuration());
        assertEquals(1, filmController.findAll().size());

        Film toUpdate = new Film();
        toUpdate.setId(createdFilm.getId());
        toUpdate.setName("newname");
        toUpdate.setDescription("newDescription");
        toUpdate.setReleaseDate(LocalDate.of(2001, 1, 1));
        toUpdate.setDuration(150L);

        Film updatedFilm = filmController.update(toUpdate);

        assertEquals("newname", updatedFilm.getName());
        assertEquals("newDescription", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2001, 1, 1), updatedFilm.getReleaseDate());
        assertEquals(150L, updatedFilm.getDuration());
        assertEquals(1, filmController.findAll().size());
    }
}
