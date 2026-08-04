package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
			       array_agg(DISTINCT g.genre_id ORDER BY g.genre_id)
			           FILTER (WHERE g.genre_id IS NOT NULL) AS genre_id,
			       array_agg(DISTINCT g.genre_name ORDER BY g.genre_id)
			           FILTER (WHERE g.genre_id IS NOT NULL) AS genre_name,
			       array_agg(DISTINCT d.director_id ORDER BY d.director_id)
			           FILTER (WHERE d.director_id IS NOT NULL) AS director_id,
			       array_agg(DISTINCT d.director_name ORDER BY d.director_id)
			           FILTER (WHERE d.director_id IS NOT NULL) AS director_name
			FROM films AS f
			JOIN mpa AS m ON f.mpa_id = m.mpa_id
			LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
			LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
			LEFT JOIN films_directors AS fd ON fd.film_id = f.film_id
			LEFT JOIN directors AS d ON d.director_id = fd.director_id
			GROUP BY f.film_id, m.mpa_id""";

	private static final String FIND_BY_ID_QUERY = """
			SELECT f.*,
			       m.mpa_name,
			       array_agg(DISTINCT g.genre_id ORDER BY g.genre_id)
			           FILTER (WHERE g.genre_id IS NOT NULL) AS genre_id,
			       array_agg(DISTINCT g.genre_name ORDER BY g.genre_id)
			           FILTER (WHERE g.genre_id IS NOT NULL) AS genre_name,
			       array_agg(DISTINCT d.director_id ORDER BY d.director_id)
			           FILTER (WHERE d.director_id IS NOT NULL) AS director_id,
			       array_agg(DISTINCT d.director_name ORDER BY d.director_id)
			           FILTER (WHERE d.director_id IS NOT NULL) AS director_name
			FROM films AS f
			JOIN mpa AS m ON f.mpa_id = m.mpa_id
			LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
			LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
			LEFT JOIN films_directors AS fd ON fd.film_id = f.film_id
			LEFT JOIN directors AS d ON d.director_id = fd.director_id
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

	private static final String GET_POPULAR_FILMS_WITH_FILTERS = """
			SELECT
			    f.*,
			    m.mpa_name,
			    array_agg(DISTINCT g.genre_id ORDER BY g.genre_id)
			           FILTER (WHERE g.genre_id IS NOT NULL) AS genre_id,
			    array_agg(DISTINCT g.genre_name ORDER BY g.genre_id)
			           FILTER (WHERE g.genre_id IS NOT NULL) AS genre_name,
			    array_agg(DISTINCT d.director_id ORDER BY d.director_id)
			           FILTER (WHERE d.director_id IS NOT NULL) AS director_id,
			    array_agg(DISTINCT d.director_name ORDER BY d.director_id)
			           FILTER (WHERE d.director_id IS NOT NULL) AS director_name
			FROM films AS f
			JOIN mpa AS m ON f.mpa_id = m.mpa_id
			LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
			LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
			LEFT JOIN films_directors AS fd ON fd.film_id = f.film_id
			LEFT JOIN directors AS d ON d.director_id = fd.director_id
			LEFT JOIN (
			    SELECT film_id, COUNT(*) AS likes_count
			    FROM films_likes
			    GROUP BY film_id
			) AS fl ON fl.film_id = f.film_id
			WHERE (CAST(? AS INTEGER) IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?)
			  AND (CAST(? AS INTEGER) IS NULL OR f.film_id IN (
			      SELECT film_id FROM films_genres WHERE genre_id = ?
			  ))
			GROUP BY f.film_id, m.mpa_id, fl.likes_count
			ORDER BY COALESCE(fl.likes_count, 0) DESC, f.film_id
			LIMIT ?""";

	private static final String DELETE_QUERY = """
			DELETE FROM films
			WHERE
			  film_id = ?""";

	private static final String GET_COMMON_FILMS = """
			SELECT films_f.*
			FROM films_likes AS fl
			LEFT JOIN
			  (SELECT film_id,
			          COUNT(*) AS likes_count
			   FROM films_likes
			   GROUP BY film_id) AS films_l ON fl.film_id = films_l.film_id
			LEFT JOIN
			  (SELECT fs.*,
			          m.mpa_name,
			          array_agg(g.genre_id
			                    ORDER BY g.genre_id) FILTER (
			                                                 WHERE g.genre_id IS NOT NULL) AS genre_id,
			          array_agg(g.genre_name
			                    ORDER BY g.genre_id) FILTER (
			                                                 WHERE g.genre_id IS NOT NULL) AS genre_name,
			          array_agg(DISTINCT d.director_id ORDER BY d.director_id)
			              FILTER (WHERE d.director_id IS NOT NULL) AS director_id,
			           array_agg(DISTINCT d.director_name ORDER BY d.director_id)
			              FILTER (WHERE d.director_id IS NOT NULL) AS director_name
			   FROM films AS fs
			   JOIN mpa AS m ON fs.mpa_id = m.mpa_id
			   LEFT JOIN films_genres AS fg ON fg.film_id = fs.film_id
			   LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
			   LEFT JOIN films_directors AS fd ON fd.film_id = fs.film_id
			   LEFT JOIN directors AS d ON d.director_id = fd.director_id
			   GROUP BY fs.film_id,
			            m.mpa_id) AS films_f ON fl.film_id =films_f.film_id
			WHERE fl.film_id IN
			    (SELECT f.film_id
			     FROM films_likes AS f
			     WHERE f.user_id = ?)
			  AND fl.user_id = ?
			ORDER BY likes_count DESC,
			         fl.film_id""";

	private static final String GET_DIRECTOR_FILMS_SORT_YEARS = """
			SELECT f.*,
			       m.mpa_name,
			       array_agg(DISTINCT g.genre_id ORDER BY g.genre_id)
			           FILTER (WHERE g.genre_id IS NOT NULL) AS genre_id,
			       array_agg(DISTINCT g.genre_name ORDER BY g.genre_id)
			           FILTER (WHERE g.genre_id IS NOT NULL) AS genre_name,
			       array_agg(DISTINCT d.director_id ORDER BY d.director_id)
			           FILTER (WHERE d.director_id IS NOT NULL) AS director_id,
			       array_agg(DISTINCT d.director_name ORDER BY d.director_id)
			           FILTER (WHERE d.director_id IS NOT NULL) AS director_name
			FROM films f
			JOIN mpa m ON f.mpa_id = m.mpa_id
			LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
			LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
			LEFT JOIN films_directors AS fd ON fd.film_id = f.film_id
			LEFT JOIN directors AS d ON d.director_id = fd.director_id
			WHERE fd.director_id = ?
			GROUP BY f.film_id, m.mpa_id
			ORDER BY f.release_date""";

	private static final String GET_DIRECTOR_FILMS_SORT_LIKES = """
			SELECT f.*,
				   m.mpa_name,
				   COUNT(l.user_id) AS rate,
				   array_agg(DISTINCT g.genre_id ORDER BY g.genre_id)
			           FILTER (WHERE g.genre_id IS NOT NULL) AS genre_id,
			       array_agg(DISTINCT g.genre_name ORDER BY g.genre_id)
			           FILTER (WHERE g.genre_id IS NOT NULL) AS genre_name,
			       array_agg(DISTINCT d.director_id ORDER BY d.director_id)
			           FILTER (WHERE d.director_id IS NOT NULL) AS director_id,
			       array_agg(DISTINCT d.director_name ORDER BY d.director_id)
			           FILTER (WHERE d.director_id IS NOT NULL) AS director_name
			FROM films f
			JOIN mpa m ON f.mpa_id = m.mpa_id
			LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
			LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
			LEFT JOIN films_directors AS fd ON fd.film_id = f.film_id
			LEFT JOIN directors AS d ON d.director_id = fd.director_id
			LEFT JOIN films_likes l ON f.film_id = l.film_id
			WHERE fd.director_id = ?
			GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, f.mpa_id, m.mpa_id, m.mpa_name
			ORDER BY rate DESC""";

	private static final String INSERT_FILMS_DIRECTORS_QUERY = """
			INSERT INTO films_directors (film_id, director_id)
			VALUES (?, ?)""";

	private static final String DELETE_FILMS_DIRECTORS = """
			DELETE FROM films_directors
			WHERE film_id = ?""";

	private static final String GET_DIRECTORS_BY_FILM_ID = """
			SELECT d.director_id,
				   d.director_name
			FROM directors d
			JOIN films_directors fd ON d.director_id = fd.director_id
			WHERE fd.film_id = ?""";

	private static final String GET_SEARCHING_FILMS = """
    SELECT f.*,
           m.mpa_name,
           array_agg(DISTINCT g.genre_id ORDER BY g.genre_id)
            FILTER (WHERE g.genre_id IS NOT NULL) AS genre_id,
           array_agg(DISTINCT g.genre_name ORDER BY g.genre_id)
            FILTER (WHERE g.genre_id IS NOT NULL) AS genre_name,
           array_agg(DISTINCT d.director_id ORDER BY d.director_id)
            FILTER (WHERE d.director_id IS NOT NULL) AS director_id,
           array_agg(DISTINCT d.director_name ORDER BY d.director_id)
            FILTER (WHERE d.director_id IS NOT NULL) AS director_name
    FROM films AS f
    JOIN mpa AS m ON f.mpa_id = m.mpa_id
    LEFT JOIN films_genres AS fg ON fg.film_id = f.film_id
    LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
    LEFT JOIN films_directors AS fd ON fd.film_id = f.film_id
    LEFT JOIN directors AS d ON d.director_id = fd.director_id
    LEFT JOIN (
        SELECT film_id, COUNT(*) AS likes_count
        FROM films_likes
        GROUP BY film_id
    ) AS fl ON fl.film_id = f.film_id
    WHERE (? = TRUE AND LOWER(f.film_name) LIKE CONCAT('%', LOWER(?), '%'))
       OR (? = TRUE AND LOWER(d.director_name) LIKE CONCAT('%', LOWER(?), '%'))
    GROUP BY f.film_id, m.mpa_id, fl.likes_count
    ORDER BY COALESCE(fl.likes_count, 0) DESC, f.film_id""";

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
		updateFilmDirectors(film);
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
		deleteFilmDirectors(film.getId());
		updateFilmDirectors(film);
		return findById(film.getId()).get();
	}

	@Override
	public void deleteFilm(Integer id) {
		int rows = jdbc.update(DELETE_QUERY, id);

		if (rows == 0) {
			throw new NotFoundException("Фильм с id = " + id + " не найден");
		}
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

	@Override
	public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
		return jdbc.query(GET_POPULAR_FILMS_WITH_FILTERS, mapper,
				year, year, genreId, genreId, count);
	}

	public List<Film> getCommonFilms(Integer userId, Integer friendId) {
		return jdbc.query(GET_COMMON_FILMS, mapper, userId, friendId);
	}

	public void updateFilmDirectors(Film film) {
		if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
			return;
		}

		jdbc.batchUpdate(
				INSERT_FILMS_DIRECTORS_QUERY,
				film.getDirectors(),
				film.getDirectors().size(),
				(ps, director) -> {
					ps.setInt(1, film.getId());
					ps.setInt(2, director.getId());
				}
		);
	}

	public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
		String sql = "year".equals(sortBy) ? GET_DIRECTOR_FILMS_SORT_YEARS : GET_DIRECTOR_FILMS_SORT_LIKES;
		return jdbc.query(sql, mapper, directorId);
	}

	private void deleteFilmDirectors(Integer filmId) {
		jdbc.update(DELETE_FILMS_DIRECTORS, filmId);
	}

	@Override
	public List<Film> searchFilms(String query, String by) {
		boolean searchTitle = by.contains("title");
		boolean searchDirector = by.contains("director");
		if (!searchTitle && !searchDirector) {
			return List.of();
		}
		return jdbc.query(GET_SEARCHING_FILMS, mapper, searchTitle, query, searchDirector, query);
	}
}