package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
	List<Director> getAllDirectors();

	Optional<Director> findDirectorById(int directorId);

	Director addDirector(Director director);

	Director update(Director director);

	void deleteDirector(Integer id);
}
