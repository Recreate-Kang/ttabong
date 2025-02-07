package com.ttabong.service.recruit;

import com.ttabong.dto.recruit.requestDto.org.DeleteRecruitsRequestDto;
import com.ttabong.dto.recruit.requestDto.org.UpdateRecruitsRequestDto;
import com.ttabong.dto.recruit.responseDto.org.DeleteRecruitsResponseDto;
import com.ttabong.dto.recruit.responseDto.org.ReadAvailableRecruitsResponseDto;
import com.ttabong.dto.recruit.responseDto.org.ReadMyRecruitsResponseDto;
import com.ttabong.dto.recruit.responseDto.org.UpdateRecruitsResponseDto;


public interface OrgRecruitService {
    //1. 메인페이지 /api/org/templates/available?cursor={templateId}&limit={limit}
    ReadAvailableRecruitsResponseDto readAvailableRecruits(Integer cursor, Integer limit);

    ReadMyRecruitsResponseDto readMyRecruits(Integer cursor, Integer limit);

    DeleteRecruitsResponseDto deleteRecruits(DeleteRecruitsRequestDto deleteRecruitDto);

    UpdateRecruitsResponseDto updateRecruit(Integer recruitId, UpdateRecruitsRequestDto requestDto);
}
