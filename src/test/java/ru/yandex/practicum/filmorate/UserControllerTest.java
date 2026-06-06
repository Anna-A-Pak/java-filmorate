package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {

    private final UserController userController = new UserController();
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void shouldAddUserWhenAllFieldsAreCorrect() {
        User user = new User();
        user.setEmail("email@gmail.com");
        user.setLogin("login");
        user.setName("Tom");
        user.setBirthday("1990-11-07");

        User addUser = userController.addUser(user);

        assertEquals(user.getEmail(), addUser.getEmail());
        assertEquals(user.getLogin(), addUser.getLogin());
        assertEquals(user.getName(), addUser.getName());
        assertEquals(user.getBirthday(), addUser.getBirthday());
    }

    @Test
    void shouldNotAddUserWithoutEmail() {
        User user = new User();
        user.setLogin("login");
        user.setName("Tom");
        user.setBirthday("1990-11-07");

        Exception e = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Email не может быть пустым", e.getMessage());
    }

    @Test
    void shouldNotAddUserWithIncorrectEmail() {
        User user = new User();
        user.setEmail("emailgmail.com");
        user.setLogin("login");
        user.setName("Tom");
        user.setBirthday("1990-11-07");

        Exception e = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Email должен содержать символ '@'", e.getMessage());
    }

    @Test
    void shouldAddUserWithoutName() {
        User user = new User();
        user.setEmail("email@gmail.com");
        user.setLogin("login");
        user.setBirthday("1990-11-07");

        User addUser = userController.addUser(user);

        assertEquals(user.getEmail(), addUser.getEmail());
        assertEquals(user.getLogin(), addUser.getLogin());
        assertEquals(user.getName(), addUser.getName());
        assertEquals(user.getBirthday(), addUser.getBirthday());
    }

    @Test
    void shouldNotAddUserWithIncorrectLogin() {
        User user = new User();
        user.setEmail("email@gmail.com");
        user.setLogin("log in");
        user.setName("Tom");
        user.setBirthday("1990-11-07");

        Exception e = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Логин не должен содержать пробелы", e.getMessage());
    }

    @Test
    void shouldNotAddUserWithoutLogin() {
        User user = new User();
        user.setEmail("email@gmail.com");
        user.setName("Tom");
        user.setBirthday("1990-11-07");

        Exception e = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Логин не может быть пустым", e.getMessage());
    }

    @Test
    void shouldNotAddUserWithoutBirthday() {
        User user = new User();
        user.setEmail("email@gmail.com");
        user.setLogin("login");
        user.setName("Tom");

        Exception e = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Дата рождения не может быть пустой", e.getMessage());
    }

    @Test
    void shouldNotAddUserWithIncorrectBirthday() {
        User user = new User();
        user.setEmail("email@gmail.com");
        user.setLogin("login");
        user.setName("Tom");
        user.setBirthday(LocalDate
                .now()
                .plusMonths(1)
                .toString());

        Exception e = assertThrows(ValidationException.class, () -> {
            userController.addUser(user);
        });
        assertEquals("Дата рождения не может быть в будущем", e.getMessage());
    }
}
