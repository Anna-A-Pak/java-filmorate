package ru.yandex.practicum.filmorate;

import jakarta.validation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerTest {

    private static UserStorage userStorage = new InMemoryUserStorage();
    private static UserService userService = new UserService(userStorage);
    private static UserController userController = new UserController(userService);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static ValidatorFactory validatorFactory;
    private static Validator validator;
    private User user1;
    private User user2;
    private User user3;
    private User user4;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userService);

        user1 = createUser("mail@gmail.com", "log1", "Bob", "1990-11-05");
        user2 = createUser("mail@mail.com", "log2", "Ross", "1988-12-11");
        user3 = createUser("mailmail@mail.com", "log3", "Ivan", "1983-10-10");
        user4 = createUser("emailmail@mail.com", "log4", "Kate", "1985-09-18");

        User addUser1 = userController.addUser(user1);
        User addUser2 = userController.addUser(user2);
        User addUser3 = userController.addUser(user3);
        User addUser4 = userController.addUser(user4);
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
        assertEquals("email", getPropertyPath(violations));
        assertEquals(NotBlank.class, getType(violations));
    }

    @Test
    void shouldNotAddUserWithIncorrectEmail() {
        User user = new User();
        user.setEmail("emailgmail.com");
        user.setLogin("login");
        user.setName("Tom");
        user.setBirthday("1990-11-07");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals("email", getPropertyPath(violations));
        assertEquals(Email.class, getType(violations));
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
        assertEquals("login", getPropertyPath(violations));
        assertEquals(NotBlank.class, getType(violations));
    }

    @Test
    void shouldNotAddUserWithoutBirthday() {
        User user = new User();
        user.setEmail("email@gmail.com");
        user.setLogin("login");
        user.setName("Tom");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals("birthday", getPropertyPath(violations));
        assertEquals(NotBlank.class, getType(violations));
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

    @Test
    void user1AndUser2ShouldBeFriends() {
        userController.addFriend(1, 2);
        assertThat(user1.getFriends())
                .containsExactlyInAnyOrder(2);

        assertThat(user2.getFriends())
                .containsExactlyInAnyOrder(1);
    }

    @Test
    void shouldReturnOnlyCommonFriends() {
        userController.addFriend(1, 2);
        userController.addFriend(1, 3);
        userController.addFriend(1, 4);
        userController.addFriend(2, 4);

        assertThat(userController.getSameFriends(1, 2))
                .containsExactlyInAnyOrder(user4);
    }

    @Test
    void shouldReturnEmptyListWhenNoCommonFriends() {
        userController.addFriend(1, 2);
        assertThat(userController.getSameFriends(1,2))
                .isEmpty();

    }

    private User createUser(String email, String login, String name, String birthDay) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthDay);
        return user;
    }

    private String getPropertyPath(Set<ConstraintViolation<User>> violations) {
        String field = "";
        for (ConstraintViolation<User> violation : violations) {
            field = violation.getPropertyPath().toString();
        }
        return field;
    }

    private Class<? extends Annotation> getType(Set<ConstraintViolation<User>> violations) {
        Class<? extends Annotation> type = null;
        for (ConstraintViolation<User> violation : violations) {
            type = violation.getConstraintDescriptor().getAnnotation().annotationType();
        }
        return type;
    }
}
