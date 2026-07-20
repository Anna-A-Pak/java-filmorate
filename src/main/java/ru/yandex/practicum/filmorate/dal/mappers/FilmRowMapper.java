package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
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
        Array genresIdArray = resultSet.getArray("genres_id");
        Array genresNameArray = resultSet.getArray("genres_name");

        if (genresIdArray != null && genresNameArray != null) {
            Object[] genresId = (Object[]) genresIdArray.getArray();
            Object[] genresName = (Object[]) genresNameArray.getArray();

            for (int i = 0; i < genresId.length; i++) {
                int genreId = ((Number) genresId[i]).intValue();
                String genreName = genresName[i].toString();

                genres.add(new Genre(genreId, genreName));
            }
        }

        film.setGenres(genres);

        return film;
    }
}
