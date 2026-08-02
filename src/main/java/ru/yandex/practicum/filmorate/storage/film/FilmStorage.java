package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
	List<Film> getAllMovies();

	Film addFilm(Film film);

	Film update(Film film);

	void deleteFilm(Integer id);

	Optional<Film> findById(int filmId);

	void addLike(Integer filmId, Integer userId);

	void deleteLike(Integer filmId, Integer userId);

    List<Film> getCommonFilms(Integer userId, Integer friendId);

    List<Film> getPopularFilms(int count, Integer genreId, Integer year);

	List<Film> getFilmsByDirector(Integer directorId, String sortBy);
}
