package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mpaMapper;

    private static final String FIND_MPA_BY_ID_QUERY = """
            SELECT
              *
            FROM
              mpa
            WHERE
              mpa_id = ?""";

    private static final String GET_ALL_MPA_QUERY = """
            SELECT
              *
            FROM mpa""";

    public Optional<Mpa> findMpaById(int mpaId) {
        try {
            Mpa result = jdbc.queryForObject(FIND_MPA_BY_ID_QUERY, mpaMapper, mpaId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public List<Mpa> getAllMpa() {
        return jdbc.query(GET_ALL_MPA_QUERY, mpaMapper);
    }
}
