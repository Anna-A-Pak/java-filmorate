package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.parse("1895-12-28");

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public List<Film> getAllMovies() {
        return filmStorage.getAllMovies();
    }

    public Film addFilm(NewFilmRequest request) {
        log.debug("Check film's fields");
        Film film = FilmMapper.mapToFilm(request);
        getMpa(film.getMpa().getId());
        checkGenres(film.getGenres());
        checkFields(film);
        Film createdFilm = filmStorage.addFilm(film);
        log.debug("Added film");
        return createdFilm;
    }

    public Film update(UpdateFilmRequest request) {
        if (request.getId() == 0) {
            log.error("Error: uninitialised id");
            throw new ValidationException("Id должен быть указан");
        }
        Film film = getFilm(request.getId());
        if (request.hasMpa()) {
            getMpa(request.getMpa().getId());
        }
        if (request.hasGenres()) {
            checkGenres(request.getGenres());
        }
        filmStorage.deleteFilmGenres(film.getId());
        Film updatedFilm = FilmMapper.updateFilmFields(film, request);

        return filmStorage.update(updatedFilm);
    }

    public void deleteFilm(Integer id) {
        filmStorage.deleteFilm(id);
    }

    public void addLike(Integer filmId, Integer userId) {
        getFilm(filmId);
        userService.getUser(userId);
        filmStorage.addLike(filmId, userId);
        log.debug("User {} liked the film {}", userId, filmId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        getFilm(filmId);
        userService.getUser(userId);
        filmStorage.deleteLike(filmId, userId);
        log.debug("User {} deleted the like for the film {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("Sorting movies by popularity");
        return filmStorage.getPopularFilms(count);
    }

    public List<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public List<Mpa> getAllMpa() {
        return filmStorage.getAllMpa();
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

    public void checkGenres(List<Genre> genres) {
        if (genres != null && !genres.isEmpty()) {
            List<Genre> genresDb = getAllGenres();
            Set<Integer> uniqueIds = new HashSet<>();

            for (Genre genre : genres) {
                boolean exists = genresDb.stream()
                        .anyMatch(genreDb ->
                                genreDb.getId() == genre.getId()
                        );

                if (!exists) {
                    throw new NotFoundException(
                            "Жанр с id = " + genre.getId() + " не найден"
                    );
                }
            }
            genres.removeIf(genre ->
                    !uniqueIds.add(genre.getId())
            );
        }
    }

    public Film getFilm(Integer filmId) {
        Optional<Film> filmOptional = filmStorage.findById(filmId);
        if (filmOptional.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        return filmOptional.get();
    }

    public Mpa getMpa(Integer mpaId) {
        Optional<Mpa> mpaOptional = filmStorage.findMpaById(mpaId);
        if (mpaOptional.isEmpty()) {
            throw new NotFoundException("MPA с id = " + mpaId + " не найден");
        }
        return mpaOptional.get();
    }

    public Genre getGenre(Integer genreId) {
        Optional<Genre> genreOptional = filmStorage.findGenreById(genreId);
        if (genreOptional.isEmpty()) {
            throw new NotFoundException("MPA с id = " + genreId + " не найден");
        }
        return genreOptional.get();
    }

}
