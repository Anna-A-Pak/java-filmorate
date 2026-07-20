package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final MpaRowMapper mpaMapper;
    private final GenreRowMapper genreMapper;

    private static final String GET_ALL_QUERY = """
            SELECT f.*,
                   m.mpa_name,
                   array_agg(g.genre_id ORDER BY g.genre_id)
                       FILTER (WHERE g.genre_id IS NOT NULL) AS genres_id,
                   array_agg(g.genre_name ORDER BY g.genre_id)
                       FILTER (WHERE g.genre_id IS NOT NULL) AS genres_name
            FROM films AS f
            JOIN mpa AS m ON f.mpa_id = m.mpa_id
            LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
            LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
            GROUP BY f.film_id, m.mpa_id""";

    private static final String FIND_BY_ID_QUERY = """
            SELECT f.*,
                   m.mpa_name,
                   array_agg(g.genre_id ORDER BY g.genre_id)
                       FILTER (WHERE g.genre_id IS NOT NULL) AS genres_id,
                   array_agg(g.genre_name ORDER BY g.genre_id)
                       FILTER (WHERE g.genre_id IS NOT NULL) AS genres_name
            FROM films AS f
            JOIN mpa AS m ON f.mpa_id = m.mpa_id
            LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
            LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
            WHERE
              f.film_id = ?
            GROUP BY f.film_id, m.mpa_id""";

    private static final String FIND_MPA_BY_ID_QUERY = """
            SELECT
              *
            FROM
              mpa
            WHERE
              mpa_id = ?""";

    private static final String FIND_GENRE_BY_ID_QUERY = """
            SELECT
              *
            FROM
              genres
            WHERE
              genre_id = ?""";

    private static final String GET_ALL_MPA_QUERY = """
            SELECT
              *
            FROM mpa""";

    private static final String GET_ALL_GENRE_QUERY = """
            SELECT
              *
            FROM genres""";

    private static final String INSERT_QUERY = """
            INSERT INTO
              films (
                film_name,
                description,
                release_date,
                duration,
                mpa_id
              )
            VALUES
              (?, ?, ?, ?, ?)""";

    private static final String INSERT_FILMS_GENRES_QUERY = """
            INSERT INTO
              films_genres (film_id, genre_id)
            VALUES
              (?, ?)""";

    private static final String UPDATE_QUERY = """
            UPDATE films
            SET
              film_name = ?,
              description = ?,
              release_date = ?,
              duration = ?,
              mpa_id = ?
            WHERE
              film_id = ?""";

    private static final String DELETE_FILMS_GENRES = """
            DELETE FROM films_genres
            WHERE
              film_id = ?""";

    private static final String INSERT_LIKES_QUERY = """
            INSERT INTO
              films_likes (film_id, user_id)
            VALUES
              (?, ?)""";

    private static final String DELETE_LIKES_QUERY = """
            DELETE FROM films_likes
            WHERE
              film_id = ?
              AND user_id = ?""";

    private static final String GET_POPULAR_FILMS = """
            SELECT
              f.film_id
            FROM
              films AS f
              LEFT JOIN films_likes AS fl ON fl.film_id = f.film_id
            GROUP BY
              f.film_id
            ORDER BY
              COUNT(fl.user_id) DESC
            LIMIT
              ?""";

    private static final String DELETE_QUERY = """
            DELETE FROM films
            WHERE
              film_id = ?""";

    public List<Film> getAllMovies() {
        return jdbc.query(GET_ALL_QUERY, mapper);
    }

    @Transactional
    public Film addFilm(Film film) {

        int id = insert(
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        addFilmGenres(film);

        return film;
    }

    public Film update(Film film) {
        update(
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        deleteFilmGenres(film.getId());
        addFilmGenres(film);

        return film;
    }

    public void deleteFilm(Integer id) {
        jdbc.update(DELETE_QUERY, id);
    }

    public Optional<Film> findById(int filmId) {
        try {
            Film result = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, filmId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Mpa> findMpaById(int mpaId) {
        try {
            Mpa result = jdbc.queryForObject(FIND_MPA_BY_ID_QUERY, mpaMapper, mpaId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Genre> findGenreById(int genreId) {
        try {
            Genre result = jdbc.queryForObject(FIND_GENRE_BY_ID_QUERY, genreMapper, genreId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public List<Mpa> getAllMpa() {
        return jdbc.query(GET_ALL_MPA_QUERY, mpaMapper);
    }

    public List<Genre> getAllGenres() {
        return jdbc.query(GET_ALL_GENRE_QUERY, genreMapper);
    }

    public void deleteFilmGenres(Integer filmId) {
        jdbc.update(DELETE_FILMS_GENRES, filmId);
    }

    public void addFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        jdbc.batchUpdate(
                INSERT_FILMS_GENRES_QUERY,
                film.getGenres(),
                film.getGenres().size(),
                (ps, genre) -> {
                    ps.setInt(1, film.getId());
                    ps.setInt(2, genre.getId());
                }
        );
    }

    public void addLike(Integer filmId, Integer userId) {
        jdbc.update(INSERT_LIKES_QUERY, filmId, userId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        jdbc.update(DELETE_LIKES_QUERY, filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Integer> filmsId = jdbc.queryForList(GET_POPULAR_FILMS, Integer.class, count);
        List<Film> films = getAllMovies();
        return filmsId.stream()
                .map(id -> films.stream()
                        .filter(film -> film.getId() == id)
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    protected int insert(Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(FilmDbStorage.INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps; }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);

        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    protected void update(Object... params) {
        int rowsUpdated = jdbc.update(FilmDbStorage.UPDATE_QUERY, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }
}
