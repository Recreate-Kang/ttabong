package com.ttabong.dto.sns.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/*
* 1. 후기 _ 자신이 쓴 후기 (미리보기) (모두 조회)
* */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyAllReviewPreviewResponseDto {
    // 후기 정보
    private Integer reviewId;
    private Integer recruitId;
    private String title;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    // 작성자 정보
    private Integer writerId;
    private String writerName;

    // 그룹 정보
    private Integer groupId;
    private String groupName;

    // 기관 정보
    private Integer orgId;
    private String orgName;

    // 이미지 리스트
    private List<String> images;
}
