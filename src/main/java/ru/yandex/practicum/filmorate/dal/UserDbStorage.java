package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class UserDbStorage extends BaseStorage implements UserStorage {

    private final UserRowMapper mapper;
    private final FilmRowMapper filmMapper;
    private final NamedParameterJdbcTemplate namedJdbc;

    public UserDbStorage(JdbcTemplate jdbc,
                         UserRowMapper mapper,
                         FilmRowMapper filmMapper,
                         NamedParameterJdbcTemplate namedJdbc) {
        super(jdbc);
        this.mapper = mapper;
        this.filmMapper = filmMapper;
        this.namedJdbc = namedJdbc;
    }

    private static final String GET_ALL_QUERY = """
            SELECT
              *
            FROM users""";

    private static final String INSERT_QUERY = """
            INSERT INTO
              users (email, login, user_name, birthday)
            VALUES
              (?, ?, ?, ?)""";

    private static final String UPDATE_QUERY = """
            UPDATE users
            SET
              email = ?,
              login = ?,
              user_name = ?,
              birthday = ?
            WHERE
              user_id = ?""";

    private static final String DELETE_QUERY = """
            DELETE FROM users
            WHERE
              user_id = ?""";

    private static final String FIND_BY_ID_QUERY = """
            SELECT
              *
            FROM
              users
            WHERE
              user_id = ?""";

    private static final String INSERT_FRIEND_QUERY = """
            INSERT INTO
              friendship (user_id, friend_id)
            VALUES
              (?, ?)""";

    private static final String DELETE_FRIEND_QUERY = """
            DELETE FROM friendship
            WHERE
              user_id = ?
              AND friend_id = ?""";

    private static final String GET_ALL_FRIENDS_QUERY = """
            SELECT
              u.user_id,
              u.email,
              u.login,
              u.user_name,
              u.birthday
            FROM
              friendship AS f
              JOIN users AS u ON f.friend_id = u.user_id
            WHERE
              f.user_id = ?""";

    private static final String GET_SAME_FRIENDS = """
            SELECT
              u.user_id,
              u.email,
              u.login,
              u.user_name,
              u.birthday
            FROM
              friendship AS f
              JOIN users AS u ON f.friend_id = u.user_id
            WHERE
              f.user_id = ?
              AND f.friend_id IN (
                SELECT
                  fs.friend_id
                FROM
                  friendship AS fs
                WHERE
                  fs.user_id = ?
              )""";

    public static final String OTHER_WITH_MAX_LIKES = """
            WITH user_likes AS (
                SELECT film_id
                FROM films_likes
                WHERE user_id = ?
            )
            SELECT fl.user_id
            FROM films_likes AS fl
            LEFT JOIN user_likes AS ul ON ul.film_id = fl.film_id
            WHERE fl.user_id <> ?
            GROUP BY fl.user_id
            HAVING COUNT(ul.film_id) > 0
               AND COUNT(*) > COUNT(ul.film_id)
            ORDER BY COUNT(ul.film_id) DESC, fl.user_id
            LIMIT 1""";

    public static final String GET_IDS_FILMS = """
            SELECT fl.film_id
            FROM films_likes AS fl
            WHERE fl.user_id = ?
              AND fl.film_id NOT IN
                (SELECT film_id
                 FROM films_likes
                 WHERE user_id = ?)""";

    public static final String GET_RECOMMENDATIONS = """
            WITH film_genres AS (
                SELECT fg.film_id,
                      array_agg(g.genre_id ORDER BY g.genre_id) AS genre_id,
                      array_agg(g.genre_name ORDER BY g.genre_id) AS genre_name
               FROM films_genres AS fg
               JOIN genres AS g ON g.genre_id = fg.genre_id
               WHERE fg.film_id IN (:idsFilms)
               GROUP BY fg.film_id
            ),
            film_directors AS (
                SELECT fd.film_id,
                      array_agg(d.director_id ORDER BY d.director_id) AS director_id,
                      array_agg(d.director_name ORDER BY d.director_id) AS director_name
               FROM films_directors AS fd
               JOIN directors AS d ON d.director_id = fd.director_id
               WHERE fd.film_id IN (:idsFilms)
               GROUP BY fd.film_id
            )
            SELECT f.*,
                   m.mpa_name,
                   fg.genre_id,
                   fg.genre_name,
                   fd.director_id,
                   fd.director_name
            FROM films AS f
            JOIN mpa AS m ON m.mpa_id = f.mpa_id
            LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id
            LEFT JOIN film_directors AS fd ON fd.film_id = f.film_id
            WHERE f.film_id IN (:idsFilms)""";

    public List<User> getAllUsers() {
        return jdbc.query(GET_ALL_QUERY, mapper);
    }

    public User addUser(User user) {
        int id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public void deleteUser(Integer id) {
        int rows = jdbc.update(DELETE_QUERY, id);

        if (rows == 0) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    public Optional<User> findById(int userId) {
        try {
            User result = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, userId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public void addFriend(Integer userId, Integer friendId) {
        jdbc.update(INSERT_FRIEND_QUERY, userId, friendId);
    }

    public void deleteFriend(User user, User friend) {
        jdbc.update(DELETE_FRIEND_QUERY, user.getId(), friend.getId());
    }

    public List<User> getAllFriends(Integer userId) {
        return jdbc.query(GET_ALL_FRIENDS_QUERY, (rs, rowNum) -> {
            User friend = new User();
            friend.setId(rs.getInt("user_id"));
            friend.setEmail(rs.getString("email"));
            friend.setLogin(rs.getString("login"));
            friend.setName(rs.getString("user_name"));
            friend.setBirthday(rs.getString("birthday"));

            return friend;
        }, userId);
    }

    public List<User> getSameFriends(User user, User friend) {
        return jdbc.query(GET_SAME_FRIENDS, (rs, rowNum) -> {
            User sameFriend = new User();
            sameFriend.setId(rs.getInt("user_id"));
            sameFriend.setEmail(rs.getString("email"));
            sameFriend.setLogin(rs.getString("login"));
            sameFriend.setName(rs.getString("user_name"));
            sameFriend.setBirthday(rs.getString("birthday"));

            return sameFriend;
        }, user.getId(), friend.getId());
    }

    public List<Film> getRecommendations(Integer id) {
        List<Integer> otherId = jdbc.queryForList(OTHER_WITH_MAX_LIKES, Integer.class, id, id);

        if (otherId.isEmpty()) {
            log.debug("There aren't suitable user for user id={}", id);
            return new ArrayList<>();
        }

        List<Integer> idsFilms = jdbc.queryForList(GET_IDS_FILMS, Integer.class, otherId.getFirst(), id);
        log.debug("There are {} of films per recommendation", idsFilms.size());

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("idsFilms", idsFilms);

        return namedJdbc.query(GET_RECOMMENDATIONS, params, filmMapper);
    }
}
