package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage extends BaseStorage implements DirectorStorage {
	private final DirectorRowMapper directorMapper;

	public DirectorDbStorage(JdbcTemplate jdbc, DirectorRowMapper directorMapper) {
		super(jdbc);
		this.directorMapper = directorMapper;
	}

	private static final String FIND_DIRECTOR_BY_ID_QUERY = """
			SELECT *
			FROM directors
			WHERE director_id = ?""";

	private static final String GET_ALL_DIRECTOR_QUERY = """
			SELECT *
			FROM directors""";

	private static final String INSERT_QUERY = """
			INSERT INTO directors (director_name)
			VALUES (?)""";

	private static final String UPDATE_QUERY = """
			UPDATE directors
			SET director_name = ?
			WHERE director_id = ?""";

	private static final String DELETE_QUERY = """
			DELETE FROM directors
			WHERE director_id = ?""";

	@Override
	public Optional<Director> findDirectorById(int directorId) {
		try {
			Director result = jdbc.queryForObject(FIND_DIRECTOR_BY_ID_QUERY, directorMapper, directorId);
			return Optional.ofNullable(result);
		} catch (EmptyResultDataAccessException ignored) {
			return Optional.empty();
		}
	}

	@Override
	public List<Director> getAllDirectors() {
		return jdbc.query(GET_ALL_DIRECTOR_QUERY, directorMapper);
	}

	@Override
	public Director addDirector(Director director) {
		int id = insert(
				INSERT_QUERY,
				director.getName()
		);
		director.setId(id);
		return director;
	}

	@Override
	public Director update(Director director) {
		update(
				UPDATE_QUERY,
				director.getName(),
				director.getId()
		);
		return director;
	}

	@Override
	public void deleteDirector(Integer id) {
		int rows = jdbc.update(DELETE_QUERY, id);
		if (rows == 0) {
			throw new NotFoundException("Режисер с id = " + id + " не найден");
		}
	}
}