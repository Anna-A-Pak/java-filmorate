package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody NewReviewRequest request) {
        return reviewService.addReview(request);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.updateReview(request);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Integer id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review findReviewById(@PathVariable Integer id) {
        return reviewService.findReviewById(id);
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Integer filmId,
                                   @RequestParam(defaultValue = "10") int count) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable Integer id,
                        @PathVariable Integer userId) {
        reviewService.putLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void putDislike(@PathVariable Integer id,
                           @PathVariable Integer userId) {
        reviewService.putDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Integer id,
                           @PathVariable Integer userId) {
        reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Integer id,
                              @PathVariable Integer userId) {
        reviewService.deleteDislike(id, userId);
    }
}
