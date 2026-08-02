package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.director.DirectorService;

import java.util.List;
@RestController
@RequestMapping("/directors")
public class DirectorController {
	private final DirectorService directorService;

	@Autowired
	public DirectorController(DirectorService directorService) {
		this.directorService = directorService;
	}

	@GetMapping
	public List<Director> getAllDirectors() {
		return directorService.getAllDirectors();
	}

	@GetMapping("/{id}")
	public Director findById(@PathVariable Integer id) {
		return directorService.getDirector(id);
	}

	@PostMapping
	public Director addDirector(@Valid @RequestBody NewDirectorRequest request) {
		return directorService.addDirector(request);
	}

	@PutMapping
	public Director update(@Valid @RequestBody UpdateDirectorRequest request) {
		return directorService.update(request);
	}

	@DeleteMapping("/{id}")
	public void deleteDirector(@PathVariable Integer id) {
		directorService.deleteDirector(id);
	}
}
