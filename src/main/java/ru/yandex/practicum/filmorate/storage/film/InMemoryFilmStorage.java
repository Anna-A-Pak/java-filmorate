package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.parse("1895-12-28");
    private final Map<Integer, Film> films = new HashMap<>();

    public List<Film> getAllMovies() {
        return new ArrayList<>(films.values());
    }

    public Film addFilm(Film film) {
        log.debug("Check film's fields");
        checkFields(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.debug("Added film");
        return film;
    }

    public Film update(Film updateFilm) {
        if (updateFilm.getId() == 0) {
            log.error("Error: uninitialised id");
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(updateFilm.getId())) {
            Film oldFilm = films.get(updateFilm.getId());
            log.debug("Check update film's fields");
            checkFields(updateFilm);
            oldFilm.setName(updateFilm.getName());
            oldFilm.setDescription(updateFilm.getDescription());
            oldFilm.setReleaseDate(updateFilm.getReleaseDate());
            oldFilm.setDuration(updateFilm.getDuration());
            log.debug("Updated film {}", oldFilm.getName());
            return oldFilm;
        }
        log.error("Error: film with id {} isn't found", updateFilm.getId());
        throw new NotFoundException("Фильм с id = " + updateFilm.getId() + " не найден");
    }

    public void deleteFilm(Integer id) {
        films.remove(id);
    }

    public Optional<Film> findById(int filmId) {
        return Optional.ofNullable(films.get(filmId));
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

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        log.debug("film's id: {}", currentMaxId + 1);
        return ++currentMaxId;
    }
}
