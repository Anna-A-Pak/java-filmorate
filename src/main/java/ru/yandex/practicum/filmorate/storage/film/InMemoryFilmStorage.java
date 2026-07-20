package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private final Map<Integer, Genre> genres = new HashMap<>();

    private final Map<Integer, Mpa> mpa = Map.of(1,new Mpa(1, "G"),
            4, new Mpa(4, "R"), 5, new Mpa(5, "NC-17"));

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
        oldFilm.setMpa(updateFilm.getMpa());
        oldFilm.setGenres(updateFilm.getGenres());
        return oldFilm;
    }

    public void deleteFilm(Integer id) {
        films.remove(id);
    }

    public Optional<Film> findById(int filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    public Optional<Mpa> findMpaById(int mpaId) {
        return Optional.ofNullable(mpa.get(mpaId));
    }

    public Optional<Genre> findGenreById(int genreId) {
        return Optional.ofNullable(genres.get(genreId));
    }

    public List<Mpa> getAllMpa() {
        return new ArrayList<>(mpa.values());
    }

    public List<Genre> getAllGenres() {
        return new ArrayList<>(genres.values());
    }

    public void deleteFilmGenres(Integer filmId) {
        findById(filmId).get().getGenres().clear();
    }

    public void addLike(Integer filmId, Integer userId) {
        films.get(filmId).getLikes().add(userId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        films.get(filmId).getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> filmsList = getAllMovies();
        return filmsList.stream()
                .sorted(Comparator.<Film>comparingInt(film -> film.getLikes().size()).reversed())
                .limit(count)
                .toList();
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
