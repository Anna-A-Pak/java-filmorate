package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseStorage implements ReviewStorage {
    private final ReviewRowMapper mapper;

    public ReviewDbStorage(JdbcTemplate jdbc, ReviewRowMapper mapper) {
        super(jdbc);
        this.mapper = mapper;
    }

    private static final String INSERT_QUERY = """
            INSERT INTO
              reviews (content, is_positive, user_id, film_id)
            VALUES
              (?, ?, ?, ?)""";

    private static final String UPDATE_QUERY = """
            UPDATE reviews
            SET
              content = ?,
              is_positive = ?,
              user_id = ?,
              film_id = ?
            WHERE
              review_id = ?""";

    private static final String DELETE_QUERY = """
            DELETE FROM reviews
            WHERE
              review_id = ?""";

    private static final String FIND_BY_ID_QUERY = """
            SELECT
              *
            FROM
              reviews
            WHERE
              review_id = ?""";

    private static final String GET_REVIEWS = """
            SELECT *
            FROM reviews AS r
            WHERE (CAST(? AS INTEGER) IS NULL
                   OR r.film_id IN
                     (SELECT film_id
                      FROM films
                      WHERE film_id = ?))
            ORDER BY r.useful DESC
            LIMIT ?""";

    private static final String UPDATE_USEFUL = """
            UPDATE reviews
            SET useful = useful + ?
            WHERE review_id = ?""";

    private static final String INSERT_USERS_RATINGS = """
            INSERT INTO
              users_ratings (review_id, user_id, is_positive)
            VALUES
              (?, ?, ?)""";

    private static final String SELECT_RATING_QUERY = """
            SELECT
              is_positive
            FROM
              users_ratings
            WHERE
              review_id = ? AND user_id = ?""";

    private static final String UPDATE_USERS_RATINGS = """
            UPDATE users_ratings
            SET is_positive = ?
            WHERE review_id = ? AND user_id = ?""";

    private static final String DELETE_USERS_RATINGS = """
            DELETE FROM users_ratings
            WHERE
              review_id = ? AND user_id = ?""";


    @Override
    public Review addReview(Review review) {
        int id = insert(
                INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId()
        );
        review.setReviewId(id);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        update(
                UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getReviewId()
        );
        return review;
    }

    @Override
    public void deleteReview(Integer id) {
        int rows = jdbc.update(DELETE_QUERY, id);

        if (rows == 0) {
            throw new NotFoundException("Отзыв с id = " + id + " не найден");
        }
    }

    @Override
    public Optional<Review> findReviewById(Integer id) {
        try {
            Review result = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getReviews(Integer filmId, int count) {
        return jdbc.query(GET_REVIEWS, mapper, filmId, filmId, count);
    }

    @Override
    public void putLike(Integer id, Integer userId) {
        if (getUserRating(id, userId).isEmpty()) {
            jdbc.update(INSERT_USERS_RATINGS, id, userId, true);
            jdbc.update(UPDATE_USEFUL, 1, id);
        } else if (!getUserRating(id, userId).getFirst()) {
            jdbc.update(UPDATE_USERS_RATINGS, id, userId, getUserRating(id, userId).getFirst());
            jdbc.update(UPDATE_USEFUL, 2, id);
        }
    }

    @Override
    public void putDislike(Integer id, Integer userId) {
        if (getUserRating(id, userId).isEmpty()) {
            jdbc.update(INSERT_USERS_RATINGS, id, userId, false);
            jdbc.update(UPDATE_USEFUL, -1, id);
        } else if (getUserRating(id, userId).getFirst()) {
            jdbc.update(UPDATE_USERS_RATINGS, id, userId, getUserRating(id, userId).getFirst());
            jdbc.update(UPDATE_USEFUL, -2, id);
        }
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        if (!getUserRating(id, userId).isEmpty() && getUserRating(id, userId).getFirst()) {
            jdbc.update(DELETE_USERS_RATINGS, id, userId);
            jdbc.update(UPDATE_USEFUL, -1, id);
        }
    }

    @Override
    public void deleteDislike(Integer id, Integer userId) {
        if (!getUserRating(id, userId).isEmpty() && !getUserRating(id, userId).getFirst()) {
            jdbc.update(DELETE_USERS_RATINGS, id, userId);
            jdbc.update(UPDATE_USEFUL, 1, id);
        }
    }

    private List<Boolean> getUserRating(Integer id, Integer userId) {
        return jdbc.query(
                SELECT_RATING_QUERY,
                (rs, rowNum) -> rs.getBoolean("is_positive"),
                id,
                userId
        );
    }
}
