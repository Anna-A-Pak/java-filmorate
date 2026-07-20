package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

public class FilmControllerTest {

    private final FilmStorage filmStorage = new InMemoryFilmStorage();
    private final UserStorage userStorage = new InMemoryUserStorage();
    private final UserService userService = new UserService(userStorage);
    private final FilmService filmService = new FilmService(filmStorage, userService);
    private final FilmController filmController = new FilmController(filmService);
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.parse("1895-12-28");
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldAddFilmWhenAllFieldsAreCorrect() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Вверх");
        film.setDescription("Приключенческое драмеди");
        film.setReleaseDate("2009-05-13");
        film.setDuration(96);
        film.setMpa(new Mpa(1, "G"));

        Film addFilm = filmController.addFilm(film);

        assertEquals(film.getName(), addFilm.getName());
        assertEquals(film.getDescription(), addFilm.getDescription());
        assertEquals(film.getReleaseDate(), addFilm.getReleaseDate());
        assertEquals(film.getDuration(), addFilm.getDuration());
    }

    @Test
    void shouldNotAddFilmWithoutName() {
        Film film = new Film();
        film.setDescription("Приключенческое драмеди");
        film.setReleaseDate("2009-05-13");
        film.setDuration(96);
        film.setMpa(new Mpa(1, "G"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals("name", getPropertyPath(violations));
        assertEquals(NotBlank.class, getType(violations));
    }

    @Test
    void shouldNotAddFilmWithNameIsBlank() {
        Film film = new Film();
        film.setName("  ");
        film.setDescription("Приключенческое драмеди");
        film.setReleaseDate("2009-05-13");
        film.setDuration(96);
        film.setMpa(new Mpa(1, "G"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals("name", getPropertyPath(violations));
        assertEquals(NotBlank.class, getType(violations));
    }

    @Test
    void shouldNotAddFilmWithoutDescription() {
        Film film = new Film();
        film.setName("Вверх");
        film.setDescription("  ");
        film.setReleaseDate("2009-05-13");
        film.setDuration(96);
        film.setMpa(new Mpa(1, "G"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals("description", getPropertyPath(violations));
        assertEquals(NotBlank.class, getType(violations));
    }

    @Test
    void shouldNotAddFilmWithDescriptionLengthMoreThen200() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Вверх");
        film.setDescription("Всю жизнь Элли хотела попасть в Южную Америку к Райскому водопаду. " +
                "Однажды они с мужем решили завести ребёнка, но после выкидыша девушка оказалась бесплодной. " +
                "Ближе к старости Карл решил реализовать её мечту попасть на Райский водопад, " +
                "однако она умерла незадолго до поездки. Карл поставил целью претворить своё обещание " +
                "в жизнь и попасть на тепуи.");
        film.setReleaseDate("2009-05-13");
        film.setDuration(96);
        film.setMpa(new Mpa(1, "G"));

        Exception e = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Превышена максимальная длина описания", e.getMessage());
    }

    @Test
    void shouldNotAddFilmWithReleaseDateLessThenMinReleaseDate() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Вверх");
        film.setDescription("Приключенческое драмеди");
        film.setReleaseDate("1894-05-13");
        film.setDuration(96);
        film.setMpa(new Mpa(1, "G"));

        Exception e = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Дата выхода фильма не может быть раньше " + MIN_RELEASE_DATE, e.getMessage());
    }

    @Test
    void shouldNotAddFilmWithDurationNil() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Вверх");
        film.setDescription("Приключенческое драмеди");
        film.setReleaseDate("2009-05-13");
        film.setDuration(0);
        film.setMpa(new Mpa(1, "G"));

        Exception e = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Продолжительность фильма не может быть пустой", e.getMessage());
    }

    @Test
    void shouldNotAddFilmWithDurationNegative() {
        Film film = new Film();
        film.setName("Вверх");
        film.setDescription("Приключенческое драмеди");
        film.setReleaseDate("2009-05-13");
        film.setDuration(-100);
        film.setMpa(new Mpa(1, "G"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals("duration", getPropertyPath(violations));
        assertEquals(Positive.class, getType(violations));
    }

    @Test
    void something() {

        Mpa mpaG = new Mpa(1, "G");
        Mpa mpaR = new Mpa(4, "R");
        Mpa mpaNC = new Mpa(5, "NC-17");

        User user1 = createUser("mail@gmail.com", "log1", "Bob", "1990-11-05");
        User user2 = createUser("mail@mail.com", "log2", "Ross", "1988-12-11");
        User user3 = createUser("mailmail@mail.com", "log3", "Ivan", "1983-10-10");
        User user4 = createUser("emailmail@mail.com", "log4", "Kate", "1985-09-18");

        User addUser1 = userStorage.addUser(user1);
        User addUser2 = userStorage.addUser(user2);
        User addUser3 = userStorage.addUser(user3);
        User addUser4 = userStorage.addUser(user4);

        NewFilmRequest newFilm1 = createFilm("Вверх", "Приключенческое драмеди",
                "2009-05-13", 96, mpaG);
        NewFilmRequest newFilm2 = createFilm("Дюна", "Научная фантастика",
                "2021-09-03", 155, mpaR);
        NewFilmRequest newfFilm3 = createFilm("Стражи Галактики", "Боевик",
                "2014-07-21", 121, mpaNC);

        Film film1 = filmController.addFilm(newFilm1);
        Film film2 = filmController.addFilm(newFilm2);
        Film film3 = filmController.addFilm(newfFilm3);

        filmController.addLike(1,1);
        filmController.addLike(2,1);
        filmController.addLike(3,1);
        filmController.addLike(3,2);
        filmController.addLike(3,3);
        filmController.addLike(3,4);
        filmController.addLike(2,4);

        assertThat(filmController.getPopularFilms(10)).containsExactly(film3, film2, film1);

    }

    private User createUser(String email, String login, String name, String birthDay) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthDay);
        return user;
    }

    private NewFilmRequest createFilm(String name, String description, String releaseDate, int duration, Mpa mpa) {
        NewFilmRequest film = new NewFilmRequest();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        film.setMpa(mpa);
        return film;
    }

    private String getPropertyPath(Set<ConstraintViolation<Film>> violations) {
        String field = "";
        for (ConstraintViolation<Film> violation : violations) {
            field = violation.getPropertyPath().toString();
        }
        return field;
    }

    private Class<? extends Annotation> getType(Set<ConstraintViolation<Film>> violations) {
        Class<? extends Annotation> type = null;
        for (ConstraintViolation<Film> violation : violations) {
            type = violation.getConstraintDescriptor().getAnnotation().annotationType();
        }
        return type;
    }
}
