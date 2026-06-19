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
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {

    private final FilmController filmController = new FilmController();
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
        Film film = new Film();
        film.setName("Вверх");
        film.setDescription("Приключенческое драмеди");
        film.setReleaseDate("2009-05-13");
        film.setDuration(96);

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

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals("description", getPropertyPath(violations));
        assertEquals(NotBlank.class, getType(violations));
    }

    @Test
    void shouldNotAddFilmWithDescriptionLengthMoreThen200() {
        Film film = new Film();
        film.setName("Вверх");
        film.setDescription("Всю жизнь Элли хотела попасть в Южную Америку к Райскому водопаду. " +
                "Однажды они с мужем решили завести ребёнка, но после выкидыша девушка оказалась бесплодной. " +
                "Ближе к старости Карл решил реализовать её мечту попасть на Райский водопад, " +
                "однако она умерла незадолго до поездки. Карл поставил целью претворить своё обещание " +
                "в жизнь и попасть на тепуи.");
        film.setReleaseDate("2009-05-13");
        film.setDuration(96);

        Exception e = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Превышена максимальная длина описания", e.getMessage());
    }

    @Test
    void shouldNotAddFilmWithReleaseDateLessThenMinReleaseDate() {
        Film film = new Film();
        film.setName("Вверх");
        film.setDescription("Приключенческое драмеди");
        film.setReleaseDate("1894-05-13");
        film.setDuration(96);

        Exception e = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Дата выхода фильма не может быть раньше " + MIN_RELEASE_DATE, e.getMessage());
    }

    @Test
    void shouldNotAddFilmWithDurationNil() {
        Film film = new Film();
        film.setName("Вверх");
        film.setDescription("Приключенческое драмеди");
        film.setReleaseDate("2009-05-13");
        film.setDuration(0);

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

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals("duration", getPropertyPath(violations));
        assertEquals(Positive.class, getType(violations));
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
