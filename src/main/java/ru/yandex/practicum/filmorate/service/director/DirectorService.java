package ru.yandex.practicum.filmorate.service.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DirectorService {
	private final DirectorStorage directorStorage;

	@Autowired
	public DirectorService(DirectorStorage directorStorage) {
		this.directorStorage = directorStorage;
	}

	public List<Director> getAllDirectors() {
		return directorStorage.getAllDirectors();
	}

	public Director getDirector(Integer directorId) {
		Optional<Director> directorOptional = directorStorage.findDirectorById(directorId);
		if (directorOptional.isEmpty()) {
			throw new NotFoundException("Режисер с id = " + directorId + " не найден");
		}
		return directorOptional.get();
	}

	public Director addDirector(NewDirectorRequest request) {
		Director director = DirectorMapper.mapToDirector(request);
		Director createdDirector = directorStorage.addDirector(director);
		log.debug("Added director with id={}", createdDirector.getId());
		return createdDirector;
	}

	public Director update(UpdateDirectorRequest request) {
		if (request.getId() == 0) {
			log.error("Error: uninitialised id");
			throw new ValidationException("Id должен быть указан");
		}
		Director updatedDirector = directorStorage.findDirectorById(request.getId())
				.map(director -> DirectorMapper.updateDirectorFields(director, request))
				.orElseThrow(() -> new NotFoundException("Режисер не найден"));
		return directorStorage.update(updatedDirector);
	}

	public void deleteDirector(Integer id) {
		directorStorage.deleteDirector(id);
	}
}