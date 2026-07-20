package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        UserDbStorage.class,
        UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbc;
    private User user1;
    private User user2;
    private User user3;
    private User user4;

    @BeforeEach
    void beforeEach() {
        jdbc.update("DELETE FROM friendship");
        jdbc.update("DELETE FROM users");

        jdbc.execute(
                "ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1"
        );
        user1 = new User();
        user1.setEmail("mail@gmail.com");
        user1.setLogin("log1");
        user1.setName("Bob");
        user1.setBirthday("1990-11-05");

        user2 = new User();
        user2.setEmail("mail@mail.com");
        user2.setLogin("log2");
        user2.setName("Tom");
        user2.setBirthday("1995-10-15");

        user3 = new User();
        user3.setEmail("email@gmail.com");
        user3.setLogin("log3");
        user3.setName("Inga");
        user3.setBirthday("1998-05-10");

        user4 = new User();
        user4.setEmail("email@mail.com");
        user4.setLogin("log4");
        user4.setName("Jenny");
        user4.setBirthday("1983-10-15");

        userStorage.addUser(user1);
        userStorage.addUser(user2);
        userStorage.addUser(user3);
        userStorage.addUser(user4);
    }

    @Test
    public void shouldFindUserById() {

        Optional<User> userOptional = userStorage.findById(user1.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", user1.getId())
                );
    }

    @Test
    void shouldReturnFourUsers() {
        List<User> users = userStorage.getAllUsers();
        assertThat(users).hasSize(4);
    }

    @Test
    void shouldReturnThreeUsers() {
        userStorage.deleteUser(user4.getId());
        List<User> users = userStorage.getAllUsers();
        assertThat(users).hasSize(3);
    }

    @Test
    void user2ShouldBeFriendUser1() {
        userStorage.addFriend(user1.getId(),user2.getId());
        List<User> friends = userStorage.getAllFriends(user1.getId());

        assertThat(friends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user2.getId());
    }

    @Test
    void shouldBeOneSameFriend() {
        userStorage.addFriend(user1.getId(),user2.getId());
        userStorage.addFriend(user1.getId(),user3.getId());
        userStorage.addFriend(user1.getId(),user4.getId());
        userStorage.addFriend(user2.getId(),user4.getId());

        List<User> sameFriends = userStorage.getSameFriends(user1, user2);

        assertThat(sameFriends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user4.getId());
    }
}
