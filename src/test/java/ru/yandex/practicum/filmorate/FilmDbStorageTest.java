package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmDbStorage.class,
        FilmRowMapper.class,
        MpaRowMapper.class,
        GenreRowMapper.class,
        UserDbStorage.class,
        UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbc;
    private Film film1;
    private Film film2;
    private Film film3;
    private User user1;
    private User user2;
    private User user3;
    private User user4;


    @BeforeEach
    void beforeEach() {
        jdbc.update("DELETE FROM films_likes");
        jdbc.update("DELETE FROM films_genres");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");

        jdbc.execute(
                "ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1"
        );
        jdbc.execute(
                "ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1"
        );

        List<Genre> genreD = List.of(new Genre(6, "Боевик"));
        List<Genre> genres = List.of(
                new Genre(1, "Комедия"),
                new Genre(6, "Боевик")
        );

        film1 = new Film();
        film1.setName("Вверх");
        film1.setDescription("Приключенческое драмеди");
        film1.setReleaseDate("2009-05-13");
        film1.setDuration(96);
        film1.setMpa(new Mpa(1, "G"));

        film2 = new Film();
        film2.setName("Дюна");
        film2.setDescription("Научная фантастика");
        film2.setReleaseDate("2021-09-03");
        film2.setDuration(155);
        film2.setMpa(new Mpa(4, "R"));
        film2.setGenres(genreD);

        film3 = new Film();
        film3.setName("Стражи Галактики");
        film3.setDescription("Боевик");
        film3.setReleaseDate("2014-07-21");
        film3.setDuration(121);
        film3.setMpa(new Mpa(5, "NC-17"));
        film3.setGenres(genres);

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);
        filmStorage.addFilm(film3);

        user1 = new User();
        user1.setEmail("mail@gmail.com");
        user1.setLogin("log1");
        user1.setName("Bob");
        user1.setBirthday("1990-11-05");

        user2 = new User();
        user2.setEmail("mail@mail.com");
        user2.setLogin("log2");
        user2.setName("Tom");
        user2.setBirthday("1995-10-15");

        user3 = new User();
        user3.setEmail("email@gmail.com");
        user3.setLogin("log3");
        user3.setName("Inga");
        user3.setBirthday("1998-05-10");

        user4 = new User();
        user4.setEmail("email@mail.com");
        user4.setLogin("log4");
        user4.setName("Jenny");
        user4.setBirthday("1983-10-15");

        userStorage.addUser(user1);
        userStorage.addUser(user2);
        userStorage.addUser(user3);
        userStorage.addUser(user4);
    }

    @Test
    public void shouldFindFilmById() {

        Optional<Film> filmOptional = filmStorage.findById(film1.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", film1.getId())
                );
    }

    @Test
    void shouldReturnThreeFilms() {
        List<Film> films = filmStorage.getAllMovies();
        assertThat(films).hasSize(3);
    }

    @Test
    void shouldBeTwoGenresInFilm() {
        List<Genre> genre = List.of(
                new Genre(6, "Боевик")

        );
        film3.setGenres(genre);
        filmStorage.update(film3);
        List<Genre> genresInFilm = film3.getGenres();
        assertThat(genresInFilm)
                .hasSize(1)
                .extracting(Genre::getId)
                .containsExactly(6);
    }

    @Test
    void shouldReturnFilmsInOrder231() {
        filmStorage.addLike(film3.getId(), user1.getId());
        filmStorage.addLike(film3.getId(), user3.getId());
        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user3.getId());
        filmStorage.addLike(film2.getId(), user4.getId());

        List<Film> popularFilms = filmStorage.getPopularFilms(3, null, null);

        assertThat(popularFilms)
                .extracting(Film::getId)
                .containsExactly(
                        film2.getId(),
                        film3.getId(),
                        film1.getId()
                );
    }
}
