package ru.yandex.practicum.filmorate.service.mpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;
import java.util.Optional;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Mpa getMpa(Integer mpaId) {
        Optional<Mpa> mpaOptional = mpaStorage.findMpaById(mpaId);
        if (mpaOptional.isEmpty()) {
            throw new NotFoundException("MPA с id = " + mpaId + " не найден");
        }
        return mpaOptional.get();
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getAllMpa();
    }
}
