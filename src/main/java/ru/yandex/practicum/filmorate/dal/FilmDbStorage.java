package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class FilmDbStorage extends BaseStorage implements FilmStorage {

    private final FilmRowMapper mapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc);
        this.mapper = mapper;
    }

    private static final String GET_ALL_QUERY = """
            SELECT f.*,
                   m.mpa_name,
                   array_agg(g.genre_id ORDER BY g.genre_id)
                       FILTER (WHERE g.genre_id IS NOT NULL) AS genre_id,
                   array_agg(g.genre_name ORDER BY g.genre_id)
                       FILTER (WHERE g.genre_id IS NOT NULL) AS genre_name
            FROM films AS f
            JOIN mpa AS m ON f.mpa_id = m.mpa_id
            LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
            LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
            GROUP BY f.film_id, m.mpa_id""";

    private static final String FIND_BY_ID_QUERY = """
            SELECT f.*,
                   m.mpa_name,
                   array_agg(g.genre_id ORDER BY g.genre_id)
                       FILTER (WHERE g.genre_id IS NOT NULL) AS genre_id,
                   array_agg(g.genre_name ORDER BY g.genre_id)
                       FILTER (WHERE g.genre_id IS NOT NULL) AS genre_name
            FROM films AS f
            JOIN mpa AS m ON f.mpa_id = m.mpa_id
            LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
            LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
            WHERE
              f.film_id = ?
            GROUP BY f.film_id, m.mpa_id""";

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
                f.*,
                m.mpa_name,
                array_agg(g.genre_id ORDER BY g.genre_id)
                    FILTER (WHERE g.genre_id IS NOT NULL) AS genre_id,
                array_agg(g.genre_name ORDER BY g.genre_id)
                    FILTER (WHERE g.genre_id IS NOT NULL) AS genre_name
            FROM films AS f
            JOIN mpa AS m ON f.mpa_id = m.mpa_id
            LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
            LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
            LEFT JOIN (
                SELECT
                    film_id,
                    COUNT(*) AS likes_count
                FROM films_likes
                GROUP BY film_id
            ) AS fl ON fl.film_id = f.film_id
            GROUP BY
                f.film_id,
                m.mpa_id,
                fl.likes_count
            ORDER BY
                COALESCE(fl.likes_count, 0) DESC,
                f.film_id
            LIMIT ?""";

    private static final String DELETE_QUERY = """
            DELETE FROM films
            WHERE
              film_id = ?""";

    private static final String GET_SEARСHING_FILMS = """
            SELECT
                f.*,
                m.mpa_name,
                array_agg(g.genre_id ORDER BY g.genre_id)
                    FILTER (WHERE g.genre_id IS NOT NULL) AS genre_id,
                array_agg(g.genre_name ORDER BY g.genre_id)
                    FILTER (WHERE g.genre_id IS NOT NULL) AS genre_name
            FROM films AS f
            JOIN mpa AS m ON f.mpa_id = m.mpa_id
            LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
            LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
            LEFT JOIN (SELECT film_id,
                              COUNT(*) AS likes_count
                       FROM films_likes
                       GROUP BY film_id) AS fl ON fl.film_id = f.film_id
            WHERE LOWER(f.film_name) LIKE CONCAT(LOWER(?), '%')
            GROUP BY
                f.film_id,
                m.mpa_id,
                fl.likes_count
            ORDER BY
                COALESCE(fl.likes_count, 0) DESC,
                f.film_id""";

    public List<Film> getAllMovies() {
        return jdbc.query(GET_ALL_QUERY, mapper);
    }

    @Transactional
    public Film addFilm(Film film) {

        int id = insert(
                INSERT_QUERY,
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

    @Transactional
    public Film update(Film film) {
        update(
                UPDATE_QUERY,
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

    private void deleteFilmGenres(Integer filmId) {
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
        return jdbc.query(GET_POPULAR_FILMS, mapper, count);
    }

    @Override
    public List<Film> searchFilms(String title) {
        List<Film> films = jdbc.query(GET_SEARСHING_FILMS, mapper, title);
        return films;
    }
}
