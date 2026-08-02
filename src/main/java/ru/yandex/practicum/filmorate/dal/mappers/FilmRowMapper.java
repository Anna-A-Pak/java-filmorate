package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class FilmRowMapper implements RowMapper<Film> {

	@Override
	public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		Mpa mpa = new Mpa(resultSet.getInt("mpa_id"), resultSet.getString("mpa_name"));
		Film film = new Film();
		film.setId(resultSet.getInt("film_id"));
		film.setName(resultSet.getString("film_name"));
		film.setDescription(resultSet.getString("description"));
		film.setReleaseDate(resultSet.getDate("release_date").toLocalDate().toString());
		film.setDuration(resultSet.getInt("duration"));
		film.setMpa(mpa);
		List<Genre> genres = new ArrayList<>();
		try {
			Array genresIdArray = resultSet.getArray("genre_id");
			Array genresNameArray = resultSet.getArray("genre_name");
			if (genresIdArray != null && genresNameArray != null) {
				Object[] genresId = (Object[]) genresIdArray.getArray();
				Object[] genresName = (Object[]) genresNameArray.getArray();
				for (int i = 0; i < genresId.length; i++) {
					int genreId = ((Number) genresId[i]).intValue();
					String genreName = genresName[i].toString();
					genres.add(new Genre(genreId, genreName));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		film.setGenres(genres);
		List<Director> directors = new ArrayList<>();
		try {
			Array directorIdArray = resultSet.getArray("director_id");
			Array directorNameArray = resultSet.getArray("director_name");
			if (directorIdArray != null && directorNameArray != null) {
				Object[] directorIds = (Object[]) directorIdArray.getArray();
				Object[] directorNames = (Object[]) directorNameArray.getArray();
				for (int i = 0; i < directorIds.length; i++) {
					if (directorIds[i] != null) {
						int directorId = ((Number) directorIds[i]).intValue();
						String directorName = directorNames[i].toString();
						directors.add(new Director(directorId, directorName));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		film.setDirectors(directors);
		return film;
	}
}