package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();

    public List<Film> getAllMovies() {
        return new ArrayList<>(films.values());
    }

    public Film addFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    public Film update(Film updateFilm) {
        Film oldFilm = films.get(updateFilm.getId());
        oldFilm.setName(updateFilm.getName());
        oldFilm.setDescription(updateFilm.getDescription());
        oldFilm.setReleaseDate(updateFilm.getReleaseDate());
        oldFilm.setDuration(updateFilm.getDuration());
        return oldFilm;
    }

    public void deleteFilm(Integer id) {
        films.remove(id);
    }

    public Optional<Film> findById(int filmId) {
        return Optional.ofNullable(films.get(filmId));
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
