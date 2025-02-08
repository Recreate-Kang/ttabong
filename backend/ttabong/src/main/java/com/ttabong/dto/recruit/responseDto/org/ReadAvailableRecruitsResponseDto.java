package com.ttabong.dto.recruit.responseDto.org;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadAvailableRecruitsResponseDto {
    private List<TemplateDetail> templates;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateDetail {
        private Template template;
        private Group group;
        private List<Recruit> recruits;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Template {
        private Integer templateId;
        private Integer categoryId;
        private String title;
        private String activityLocation;
        private String status;
        private String imageId;
        private String contactName;
        private String contactPhone;
        private String description;
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Group {
        private Integer groupId;
        private String groupName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Recruit {
        private Integer recruitId;
        private LocalDateTime deadline;
        private LocalDate activityDate;
        private Integer activityStart; // 시간은 4자리 숫자로 표현 (1000, 1400)
        private Integer activityEnd;
        private Integer maxVolunteer;
        private Integer participateVolCount;
        private String status;
        private LocalDateTime updatedAt;
        private LocalDateTime createdAt;
    }
}