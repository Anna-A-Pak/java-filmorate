package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FilmService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    public static final int MAX_RECORD = 10;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAllMovies() {
        return filmStorage.getAllMovies();
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public void deleteFilm(Integer id) {
        filmStorage.deleteFilm(id);
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilm(filmId);
        film.getLikes().add(getUser(userId).getId());
        log.debug("User {} liked the film {}", userId, filmId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        Film film = getFilm(filmId);
        film.getLikes().remove(getUser(userId).getId());
        log.debug("User {} deleted the like for the film {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        int maxRecord = MAX_RECORD;
        if (count != 0) {
            maxRecord = count;
        }

        List<Film> films = filmStorage.getAllMovies();
        log.debug("Sorting movies by popularity");
        return films.stream()
                .sorted(Comparator.<Film>comparingInt(film -> film.getLikes().size()).reversed())
                .limit(maxRecord)
                .toList();
    }

    public Film getFilm(Integer filmId) {
        Optional<Film> filmOptional = filmStorage.findById(filmId);
        if (filmOptional.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        return filmOptional.get();
    }

    private User getUser(Integer userId) {
        Optional<User> userOptional = userStorage.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        return userOptional.get();
    }
}
