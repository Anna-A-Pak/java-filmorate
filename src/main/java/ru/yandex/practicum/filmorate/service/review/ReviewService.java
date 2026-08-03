package ru.yandex.practicum.filmorate.service.review;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewsMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;

    public ReviewService(ReviewStorage reviewStorage,
                         FilmService filmService,
                         UserService userService) {
        this.reviewStorage = reviewStorage;
        this.filmService = filmService;
        this.userService = userService;
    }

    public Review addReview(NewReviewRequest request) {
        filmService.getFilm(request.getFilmId());
        userService.getUser(request.getUserId());
        Review review = ReviewsMapper.mapToReview(request);
        return reviewStorage.addReview(review);
    }

    public Review updateReview(UpdateReviewRequest request) {
        if (request.getReviewId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        Review review = findReviewById(request.getReviewId());
        Review updatedReview = ReviewsMapper.updateReviewFields(review, request);

        return reviewStorage.updateReview(updatedReview);
    }

    public void deleteReview(Integer id) {
        reviewStorage.deleteReview(id);
    }

    public Review findReviewById(Integer reviewId) {
        Optional<Review> reviewOptional = reviewStorage.findReviewById(reviewId);
        if (reviewOptional.isEmpty()) {
            throw new NotFoundException("Отзыв с id = " + reviewId + " не найден");
        }
        return reviewOptional.get();
    }

    public List<Review> getReviews(Integer filmId, int count) {
        return reviewStorage.getReviews(filmId, count);
    }

    public void putLike(Integer id, Integer userId) {
        reviewStorage.putLike(id, userId);
    }

    public void putDislike(Integer id, Integer userId) {
        reviewStorage.putDislike(id, userId);
    }

    public void deleteLike(Integer id, Integer userId) {
        reviewStorage.deleteLike(id, userId);
    }

    public void deleteDislike(Integer id, Integer userId) {
        reviewStorage.deleteDislike(id, userId);
    }
}
