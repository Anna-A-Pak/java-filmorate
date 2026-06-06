package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {

    private final FilmController filmController = new FilmController();
    public static final LocalDate MIN_RELEASE_DATE = LocalDate.parse("1895-12-28");

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

        Exception e = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Название фильма не может быть пустым", e.getMessage());
    }

    @Test
    void shouldNotAddFilmWithNameIsBlank() {
        Film film = new Film();
        film.setName("  ");
        film.setDescription("Приключенческое драмеди");
        film.setReleaseDate("2009-05-13");
        film.setDuration(96);

        Exception e = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Название фильма не может быть пустым", e.getMessage());
    }

    @Test
    void shouldNotAddFilmWithoutDescription() {
        Film film = new Film();
        film.setName("Вверх");
        film.setDescription("  ");
        film.setReleaseDate("2009-05-13");
        film.setDuration(96);

        Exception e = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Описание фильма не может быть пустым", e.getMessage());
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

        Exception e = assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
        assertEquals("Продолжительность фильма не может быть отрицательной", e.getMessage());
    }
}
