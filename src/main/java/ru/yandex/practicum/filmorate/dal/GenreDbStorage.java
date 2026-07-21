package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreMapper;

    private static final String FIND_GENRE_BY_ID_QUERY = """
            SELECT
              *
            FROM
              genres
            WHERE
              genre_id = ?""";

    private static final String GET_ALL_GENRE_QUERY = """
            SELECT
              *
            FROM genres""";

    public Optional<Genre> findGenreById(int genreId) {
        try {
            Genre result = jdbc.queryForObject(FIND_GENRE_BY_ID_QUERY, genreMapper, genreId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public List<Genre> getAllGenres() {
        return jdbc.query(GET_ALL_GENRE_QUERY, genreMapper);
    }
}
