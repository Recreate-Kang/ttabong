package com.ttabong.controller.sns;

import com.ttabong.dto.sns.request.ReviewCreateRequestDto;
import com.ttabong.dto.sns.response.ReviewCreateResponseDto;
import com.ttabong.dto.sns.response.ReviewDeleteResponseDto;
import com.ttabong.service.sns.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    /* 후기 생성 */
    @PostMapping
    public ResponseEntity<ReviewCreateResponseDto> createReview(@RequestBody ReviewCreateRequestDto requestDto) {
        ReviewCreateResponseDto response = reviewService.createReview(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /* 후기 삭제 */
    @PatchMapping("/{reviewId}/delete")
    public ResponseEntity<ReviewDeleteResponseDto> deleteReview(@PathVariable Long reviewId) {
        ReviewDeleteResponseDto response = reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(response);
    }
}
