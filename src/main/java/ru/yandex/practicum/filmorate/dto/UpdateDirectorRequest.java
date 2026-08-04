package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class UpdateDirectorRequest {
	private int id;
	private String name;

	public boolean hasName() {
		return ! (name == null || name.isBlank());
	}
}
