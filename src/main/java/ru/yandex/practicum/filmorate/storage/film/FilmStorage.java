package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> getAllMovies();

    Film addFilm(Film film);

    Film update(Film film);

    void deleteFilm(Integer id);

    Optional<Film> findById(int filmId);

    Optional<Mpa> findMpaById(int mpaId);

    Optional<Genre> findGenreById(int genreId);

    List<Mpa> getAllMpa();

    List<Genre> getAllGenres();

    void deleteFilmGenres(Integer filmId);

    void addLike(Integer filmId, Integer userId);

    void deleteLike(Integer filmId, Integer userId);

    List<Film> getPopularFilms(int count);
}
