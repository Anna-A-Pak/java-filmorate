package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Integer id);

    Optional<Review> findReviewById(Integer id);

    List<Review> getReviews(Integer filmId, int count);

    void putLike(Integer id, Integer userId);

    void putDislike(Integer id, Integer userId);

    void deleteLike(Integer id, Integer userId);

    void deleteDislike(Integer id, Integer userId);
}
