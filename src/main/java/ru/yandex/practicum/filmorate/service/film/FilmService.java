package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.parse("1895-12-28");

    public List<Film> getAllMovies() {
        return filmStorage.getAllMovies();
    }

    public Film addFilm(Film film) {
        log.debug("Check film's fields");
        checkFields(film);
        Film createdFilm = filmStorage.addFilm(film);
        log.debug("Added film");
        return createdFilm;
    }

    public Film update(Film film) {
        if (film.getId() == 0) {
            log.error("Error: uninitialised id");
            throw new ValidationException("Id должен быть указан");
        }
        if (filmStorage.findById(film.getId()).isPresent()) {
            log.debug("Check update film's fields");
            checkFields(film);
            Film updateFilm = filmStorage.update(film);
            log.debug("Updated film {}", film.getName());
            return updateFilm;
        }
        log.error("Error: film with id {} isn't found", film.getId());
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
    }

    public void deleteFilm(Integer id) {
        filmStorage.deleteFilm(id);
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilm(filmId);
        film.getLikes().add(userService.getUser(userId).getId());
        log.debug("User {} liked the film {}", userId, filmId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        Film film = getFilm(filmId);
        film.getLikes().remove(userService.getUser(userId).getId());
        log.debug("User {} deleted the like for the film {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> films = filmStorage.getAllMovies();
        log.debug("Sorting movies by popularity");
        return films.stream()
                .sorted(Comparator.<Film>comparingInt(film -> film.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    private void checkFields(Film film) {
        if (film.getDescription().length() > 200) {
            log.error("Error: maximum length exceeded");
            throw new ValidationException("Превышена максимальная длина описания");
        }
        LocalDate dateFilm = LocalDate.parse(film.getReleaseDate(), FORMATTER);
        if (dateFilm.isBefore(MIN_RELEASE_DATE)) {
            log.error("Error: incorrect release date");
            throw new ValidationException("Дата выхода фильма не может быть раньше " + MIN_RELEASE_DATE);
        }
        if (film.getDuration() == 0) {
            log.error("Error: uninitialised film's duration");
            throw new ValidationException("Продолжительность фильма не может быть пустой");
        }
    }

    public Film getFilm(Integer filmId) {
        Optional<Film> filmOptional = filmStorage.findById(filmId);
        if (filmOptional.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        return filmOptional.get();
    }
}
