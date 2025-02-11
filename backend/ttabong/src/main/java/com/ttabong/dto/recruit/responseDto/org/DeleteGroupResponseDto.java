package com.ttabong.dto.recruit.responseDto.org;

import com.ttabong.dto.GlobalBaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteGroupResponseDto implements GlobalBaseDto {
    private String message;
    private Integer groupId;
    private Integer orgId;
}
