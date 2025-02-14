package com.ttabong.dto.recruit.responseDto.vol;

import com.ttabong.entity.recruit.Application;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyApplicationsResponseDto {
    private Integer applicationId;
    private String status;
    private Boolean evaluationDone;
    private Instant createdAt;
    private TemplateDto template;
    private GroupDto group;
    private RecruitDto recruit;

    public static MyApplicationsResponseDto from(Application application) {
        return MyApplicationsResponseDto.builder()
                .applicationId(application.getId())
                .status(application.getStatus())
                .evaluationDone(application.getEvaluationDone())
                .createdAt(application.getCreatedAt())
                .template(TemplateDto.from(application.getRecruit().getTemplate()))
                .group(GroupDto.from(application.getRecruit().getTemplate().getGroup()))
                .recruit(RecruitDto.from(application.getRecruit()))
                .build();
    }
}
