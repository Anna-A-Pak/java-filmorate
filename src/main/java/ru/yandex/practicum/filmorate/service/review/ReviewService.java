package ru.yandex.practicum.filmorate.service.review;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewsMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.event.EventService;
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
    private final EventService eventService;

    public ReviewService(ReviewStorage reviewStorage,
                         FilmService filmService,
                         UserService userService,
                         EventService eventService) {
        this.reviewStorage = reviewStorage;
        this.filmService = filmService;
        this.userService = userService;
        this.eventService = eventService;
    }

    @Transactional
    public Review addReview(NewReviewRequest request) {
        filmService.getFilm(request.getFilmId());
        userService.getUser(request.getUserId());
        Review review = ReviewsMapper.mapToReview(request);
        Review newReview = reviewStorage.addReview(review);
        eventService.addEvent(newReview.getUserId(), EventType.REVIEW, Operation.ADD, newReview.getReviewId());
        return newReview;
    }

    @Transactional
    public Review updateReview(UpdateReviewRequest request) {
        if (request.getReviewId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        Review review = findReviewById(request.getReviewId());
        Review updatedReview = reviewStorage.updateReview(ReviewsMapper.updateReviewFields(review, request));
        eventService.addEvent(updatedReview.getUserId(), EventType.REVIEW,
                Operation.UPDATE, updatedReview.getReviewId());
        return updatedReview;
    }

    @Transactional
    public void deleteReview(Integer id) {
        Review review = findReviewById(id);
        reviewStorage.deleteReview(id);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, review.getReviewId());
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
        userService.getUser(userId);
        findReviewById(id);
        reviewStorage.putLike(id, userId);

    }

    public void putDislike(Integer id, Integer userId) {
        userService.getUser(userId);
        findReviewById(id);
        reviewStorage.putDislike(id, userId);
    }


    public void deleteLike(Integer id, Integer userId) {
        userService.getUser(userId);
        findReviewById(id);
        reviewStorage.deleteLike(id, userId);
    }

    public void deleteDislike(Integer id, Integer userId) {
        userService.getUser(userId);
        findReviewById(id);
        reviewStorage.deleteDislike(id, userId);
    }
}
