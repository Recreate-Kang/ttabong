package com.ttabong.dto.recruit.responseDto.org;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGroupResponseDto {
    private String message;
    private Integer groupId;
    private Integer orgId;
}
