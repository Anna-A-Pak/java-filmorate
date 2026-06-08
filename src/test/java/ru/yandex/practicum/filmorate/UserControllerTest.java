package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {

    private final UserController userController = new UserController();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

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

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        List<String> textError = getTextError(violations);
        assertEquals("email не должно быть пустым", textError.getFirst() + " " + textError.getLast());
    }

    @Test
    void shouldNotAddUserWithIncorrectEmail() {
        User user = new User();
        user.setEmail("emailgmail.com");
        user.setLogin("login");
        user.setName("Tom");
        user.setBirthday("1990-11-07");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        List<String> textError = getTextError(violations);
        assertEquals("email должно иметь формат адреса электронной почты",
                textError.getFirst() + " " + textError.getLast());
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

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        List<String> textError = getTextError(violations);
        assertEquals("login не должно быть пустым", textError.getFirst() + " " + textError.getLast());
    }

    @Test
    void shouldNotAddUserWithoutBirthday() {
        User user = new User();
        user.setEmail("email@gmail.com");
        user.setLogin("login");
        user.setName("Tom");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        List<String> textError = getTextError(violations);
        assertEquals("birthday не должно быть пустым", textError.getFirst() + " " + textError.getLast());
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

    private List<String> getTextError(Set<ConstraintViolation<User>> violations) {
        List<String> textError = new ArrayList<>();
        String field = "";
        String message = "";
        for (ConstraintViolation<User> violation : violations) {
            field = violation.getPropertyPath().toString();
            textError.add(field);
            message = violation.getMessage();
            textError.add(message);
        }
        return textError;
    }
}
