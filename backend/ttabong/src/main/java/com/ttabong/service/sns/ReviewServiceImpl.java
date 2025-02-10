package com.ttabong.service.sns;

import com.ttabong.dto.sns.request.ReviewCreateRequestDto;
import com.ttabong.dto.sns.request.ReviewEditRequestDto;
import com.ttabong.dto.sns.request.ReviewVisibilitySettingRequestDto;
import com.ttabong.dto.sns.response.*;
import com.ttabong.entity.recruit.Recruit;
import com.ttabong.entity.recruit.Template;
import com.ttabong.entity.sns.Review;
import com.ttabong.entity.sns.ReviewImage;
import com.ttabong.entity.user.Organization;
import com.ttabong.entity.user.User;
import com.ttabong.repository.recruit.RecruitRepository;
import com.ttabong.repository.recruit.TemplateRepository;
import com.ttabong.repository.sns.ReviewImageRepository;
import com.ttabong.repository.sns.ReviewRepository;
import com.ttabong.repository.user.OrganizationRepository;
import com.ttabong.repository.user.UserRepository;
import com.ttabong.util.CacheUtil;
import com.ttabong.util.ImageUtil;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final RecruitRepository recruitRepository;
    private final TemplateRepository templateRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final CacheUtil cacheUtil;
    private final ImageUtil imageUtil;
    private final MinioClient minioClient;

    // TODO: parent-review-id 설정하는거 해야함.
    @Transactional
    @Override
    public ReviewCreateResponseDto createReview(ReviewCreateRequestDto requestDto) {
        final Organization organization = organizationRepository.findById(requestDto.getOrgId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        final User writer = userRepository.findById(requestDto.getWriterId())
                .orElseThrow(() -> new RuntimeException("Writer not found"));

        final Recruit recruit = recruitRepository.findById(requestDto.getRecruitId())
                .orElseThrow(() -> new RuntimeException("Recruit not found"));

        // recruit_id로 template_id 조회
        final Template template = templateRepository.findById(recruit.getTemplate().getId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        // template_id로 group_id 조회
        final Integer groupId = template.getGroup().getId();

        final Review review = Review.builder()
                .recruit(recruit)
                .org(organization)
                .writer(writer)
                .groupId(groupId)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .isPublic(requestDto.getIsPublic())
                .imgCount(requestDto.getImageCount())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        reviewRepository.save(review);

        // 1: 미리 10개의 이미지 슬롯 생성 (초기화)
        List<ReviewImage> imageSlots = IntStream.range(0, 10)
                .mapToObj(i -> ReviewImage.builder()
                        .review(review)
                        .template(template)
                        .imageUrl(null)  // Presigned URL이 아니라, 객체명을 저장할 예정
                        .isThumbnail(i == 0)  // 첫 번째 이미지만 썸네일로 설정
                        .isDeleted(false)
                        .createdAt(Instant.now())
                        .build())
                .collect(Collectors.toList());
        reviewImageRepository.saveAll(imageSlots); // 미리 저장

        // 2.Presigned URL을 기반으로 실제 객체명(objectPath) 업데이트
        final List<String> uploadedImages = requestDto.getUploadedImages();
        IntStream.range(0, uploadedImages.size()).forEach(i -> {
            final String objectPath = cacheUtil.findObjectPath(uploadedImages.get(i));
            if (objectPath == null) {
                throw new RuntimeException("Invalid or expired presigned URL");
            }

            // 기존 슬롯 업데이트 (Presigned URL이 아니라, objectPath를 저장)
            ReviewImage imageSlot = imageSlots.get(i);
            imageSlot.setImageUrl(objectPath);
            imageSlot.setThumbnail(i == 0);
            reviewImageRepository.save(imageSlot);
        });

        return ReviewCreateResponseDto.builder()
                .message("Review created successfully")
                .reviewId(review.getId())
                .writerId(writer.getId())
                .uploadedImages(requestDto.getUploadedImages())  // 그대로 반환 (objectPath 아님)
                .build();
    }

    @Override
    @Transactional
    public ReviewEditStartResponseDto startReviewEdit(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 리뷰가 없습니다"));

        // 2️.기존 업로드된 이미지 가져오기
        List<String> existingImages = reviewImageRepository.findByReviewId(reviewId).stream()
                .filter(image -> image.getImageUrl() != null)
                .map(image -> {
                    try {
                        return imageUtil.getPresignedDownloadUrl(image.getImageUrl());
                    } catch (Exception e) {
                        throw new RuntimeException("presigned URL 생성에 실패", e);
                    }
                })
                .collect(Collectors.toList());

        // 3️. 새로 업로드할 Presigned URL 10개 생성
        List<String> newPresignedUrls = IntStream.range(0, 10)
                .mapToObj(i -> {
                    String objectName = "review-images/" + UUID.randomUUID() + ".webp";
                    String presignedUrl = imageUtil.getPresignedUploadUrl(objectName);
                    cacheUtil.mapTempPresignedUrlwithObjectPath(presignedUrl, objectName);
                    return presignedUrl;
                })
                .collect(Collectors.toList());

        // 4️. cacheId 발급 (랜덤 UUID 사용)
        Integer cacheId = UUID.randomUUID().hashCode();

        return ReviewEditStartResponseDto.builder()
                .cacheId(cacheId)
                .writerId(review.getWriter().getId())
                .title(review.getTitle())
                .content(review.getContent())
                .isPublic(review.getIsPublic())
                .getImages(existingImages)
                .presignedUrl(newPresignedUrls)
                .build();
    }

    @Override
    @Transactional
    public ReviewEditResponseDto updateReview(Integer reviewId, ReviewEditRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // 기존 후기 정보 업데이트
        Review updatedReview = review.toBuilder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .isPublic(requestDto.getIsPublic())
                .updatedAt(Instant.now())
                .build();
        reviewRepository.save(updatedReview);

        // 기존 10개의 `ReviewImage` 데이터 가져오기 (순서 보장_id순서대로 하기)
        List<ReviewImage> existingImages = reviewImageRepository.findByReviewIdOrderByIdAsc(reviewId);

        // 기존 이미지가 없을 경우, 10개 슬롯 자동 생성
        if (existingImages.isEmpty()) {
            existingImages = IntStream.rangeClosed(1, 10)
                    .mapToObj(i -> ReviewImage.builder()
                            .review(review)
                            .imageUrl(null)  // 기본값은 null
                            .isDeleted(false)
                            .createdAt(Instant.now())
                            .build())
                    .collect(Collectors.toList());

            reviewImageRepository.saveAll(existingImages);
        }

        // 새로운 Presigned URL 리스트 (최대 10개 제한)
        List<String> presignedUrls = requestDto.getPresignedUrl();
        int newSize = Math.min(presignedUrls.size(), 10);  // 최대 10개까지만 허용

        // 기존 `ReviewImage` 슬롯을 재사용하여 업데이트
        for (int i = 0; i < 10; i++) {
            ReviewImage imageSlot = existingImages.get(i);

            if (i < newSize) {  // 새로운 이미지로 업데이트
                String objectPath = cacheUtil.findObjectPath(presignedUrls.get(i));
                if (objectPath == null) {
                    throw new RuntimeException("유효하지 않은 presigned url 입니다");
                }

                // 기존 MinIO 파일 삭제 후 새로운 파일로 교체
                try {
                    if (imageSlot.getImageUrl() != null) { // 기존 이미지가 있을 경우 삭제
                        minioClient.removeObject(
                                RemoveObjectArgs.builder()
                                        .bucket("ttabong-bucket")
                                        .object(imageSlot.getImageUrl())
                                        .build()
                        );
                    }
                } catch (Exception e) {
                    throw new RuntimeException("미니오에서 기존 이미지 삭제 실패", e);
                }

                // imageUrl 업데이트
                imageSlot = imageSlot.toBuilder()
                        .imageUrl(objectPath)
                        .isDeleted(false)
                        .build();
            } else {  // 새로운 이미지가 없는 경우, 기존 슬롯 초기화
                imageSlot = imageSlot.toBuilder()
                        .imageUrl(null)  // 기존 이미지 경로 제거
                        .isDeleted(false)  // 여전히 사용 가능하도록 유지
                        .build();
            }
            reviewImageRepository.save(imageSlot);
        }

        return ReviewEditResponseDto.builder()
                .cacheId(requestDto.getCacheId())
                .title(updatedReview.getTitle())
                .content(updatedReview.getContent())
                .isPublic(updatedReview.getIsPublic())
                .imageCount(newSize)
                .build();
    }

    /* 삭제 */
    @Transactional
    @Override
    public ReviewDeleteResponseDto deleteReview(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다. id: " + reviewId));

        Review updatedReview = review.toBuilder()
                .isDeleted(true)
                .build();

        reviewRepository.save(updatedReview);

        return new ReviewDeleteResponseDto("삭제 성공하였습니다.", updatedReview.getId(), updatedReview.getTitle(), updatedReview.getContent());
    }

    @Override
    public ReviewVisibilitySettingResponseDto updateVisibility(Integer reviewId, ReviewVisibilitySettingRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다. id: " + reviewId));

        Review updatedReviewVisibility = review.toBuilder()
                .isPublic(!review.getIsPublic())
                .updatedAt(Instant.now())
                .build();

        reviewRepository.save(updatedReviewVisibility);

        LocalDateTime updateTime = updatedReviewVisibility.getUpdatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();

        return new ReviewVisibilitySettingResponseDto(
                "공개 여부를 수정했습니다",
                reviewId,
                updatedReviewVisibility.getIsPublic(),
                updateTime
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<AllReviewPreviewResponseDto> readAllReviews(Integer cursor, Integer limit) {
        List<Review> reviews = reviewRepository.findAllReviews(cursor, PageRequest.of(0, limit));

        return reviews.stream()
                .map(review -> AllReviewPreviewResponseDto.builder()
                        .reviewId(review.getId())
                        .recruitId(review.getRecruit() != null ? review.getRecruit().getId() : null)
                        .title(review.getTitle())
                        .content(review.getContent())
                        .isDeleted(review.getIsDeleted())
                        .updatedAt(review.getUpdatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()) // Instant → LocalDateTime 변환
                        .createdAt(review.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()) // Instant → LocalDateTime 변환
                        .writerId(review.getWriter() != null ? review.getWriter().getId() : null)
                        .writerName(review.getWriter() != null ? review.getWriter().getName() : null)
                        .groupId(review.getGroupId())
                        .groupName("봉사 그룹") // 그룹 이름이 없어서 임시 값
                        .orgId(review.getOrg().getId())
                        .orgName(review.getOrg().getOrgName())
                        .images(review.getReviewComments().stream()
                                .flatMap(c -> c.getReview().getReviewComments().stream()
                                        .map(comment -> comment.getWriter().getProfileImage()))
                                .collect(Collectors.toList())) // 이미지 리스트 생성
                        .build())
                .collect(Collectors.toList()); // 리스트로 반환
    }


}
