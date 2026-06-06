package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final LocalDate MIN_RELEASE_DATE = LocalDate.parse("1895-12-28");
    private final Map<Integer, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.trace("Check film's fields");
        checkFields(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.trace("Added film");
        return film;
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        log.debug("film's id: {}", currentMaxId + 1);
        return ++currentMaxId;
    }

    @PutMapping
    public Film update(@RequestBody Film updateFilm) {
        if (updateFilm.getId() == 0) {
            log.error("Error: uninitialised id");
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(updateFilm.getId())) {
            Film oldFilm = films.get(updateFilm.getId());
            log.trace("Check update film's fields");
            checkFields(updateFilm);
            oldFilm.setName(updateFilm.getName());
            oldFilm.setDescription(updateFilm.getDescription());
            oldFilm.setReleaseDate(updateFilm.getReleaseDate());
            oldFilm.setDuration(updateFilm.getDuration());
            log.trace("Updated film {}", oldFilm.getName());
            return oldFilm;
        }
        log.error("Error: film with id {} isn't found", updateFilm.getId());
        throw new NotFoundException("Фильм с id = " + updateFilm.getId() + " не найден");
    }

    @GetMapping
    public List<Film> getAllMovies() {
        return new ArrayList<>(films.values());
    }

    private void checkFields(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Error: uninitialised film's name");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().isBlank()) {
            log.error("Error: uninitialised film's description");
            throw new ValidationException("Описание фильма не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.error("Error: maximum length exceeded");
            throw new ValidationException("Превышена максимальная длина описания");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBlank()) {
            log.error("Error: uninitialised film's release date");
            throw new ValidationException("Дата выхода не может быть пустой");
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
        if (film.getDuration() < 0) {
            log.error("Error: negative duration value");
            throw new ValidationException("Продолжительность фильма не может быть отрицательной");
        }
    }
}
