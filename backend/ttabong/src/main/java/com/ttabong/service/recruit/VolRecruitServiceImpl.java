package com.ttabong.service.recruit;

import com.ttabong.dto.recruit.responseDto.vol.*;
import com.ttabong.dto.user.OrganizationDto;
import com.ttabong.entity.recruit.Application;
import com.ttabong.entity.recruit.Recruit;
import com.ttabong.entity.recruit.Template;
import com.ttabong.entity.recruit.VolunteerReaction;
import com.ttabong.entity.user.Volunteer;
import com.ttabong.exception.ConflictException;
import com.ttabong.exception.NotFoundException;
import com.ttabong.repository.recruit.ApplicationRepository;
import com.ttabong.repository.recruit.RecruitRepository;
import com.ttabong.repository.recruit.TemplateRepository;
import com.ttabong.repository.recruit.VolunteerReactionRepository;
import com.ttabong.repository.user.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VolRecruitServiceImpl implements VolRecruitService {

    private final RecruitRepository recruitRepository;
    private final TemplateRepository templateRepository;
    private final VolunteerRepository volunteerRepository;
    private final ApplicationRepository applicationRepository;
    private final VolunteerReactionRepository reactionRepository;

    @Autowired
    public VolRecruitServiceImpl(
            RecruitRepository recruitRepository,
            TemplateRepository templateRepository,
            VolunteerRepository volunteerRepository,
            ApplicationRepository applicationRepository,
            VolunteerReactionRepository reactionRepository) {
        this.recruitRepository = recruitRepository;
        this.templateRepository = templateRepository;
        this.volunteerRepository = volunteerRepository;
        this.applicationRepository = applicationRepository;
        this.reactionRepository = reactionRepository;
    }

    // 1. 모집 공고 리스트 조회
    @Override
    public ReadVolRecruitsListResponseDto getTemplates(Integer cursor, Integer limit) {
        List<Template> templates = templateRepository.findTemplatesAfterCursor(cursor, limit);

        List<ReadVolRecruitsListResponseDto.TemplateWrapper> templateDetails = templates.stream()
                .map(template -> new ReadVolRecruitsListResponseDto.TemplateWrapper(
                        TemplateDto.from(template),
                        GroupDto.from(template.getGroup()),
                        template.getOrg() != null ? OrganizationDto.from(template.getOrg()) : null
                ))
                .collect(Collectors.toList());

        return new ReadVolRecruitsListResponseDto(templateDetails);
    }

    // 2. 특정 모집 공고 상세 조회
    @Override
    public ReadRecruitDetailResponseDto getTemplateById(Integer templateId) {
        return templateRepository.findByIdAndIsDeletedFalse(templateId)
                .map(ReadRecruitDetailResponseDto::from)
                .orElseThrow(() -> new NotFoundException("해당 모집 공고를 찾을 수 없습니다."));
    }

    // 3. 모집 공고 신청
    @Override
    public Application applyRecruit(int userId, int recruitId) {

        if (applicationRepository.existsByVolunteerUserIdAndRecruitId(userId, recruitId)) {
            throw new ConflictException("이미 신청한 모집 공고입니다.");
        }

        Volunteer volunteer = volunteerRepository.findByUserIdAndUserIsDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("봉사자를 찾을 수 없습니다."));

        Recruit recruit = recruitRepository.findByIdAndIsDeletedFalse(recruitId)
                .orElseThrow(() -> new NotFoundException("봉사 공고를 찾을 수 없습니다."));


        Application application = Application.builder()
                .volunteer(volunteer)
                .recruit(recruit)
                .status("PENDING")
                .evaluationDone(false)
                .isDeleted(false)
                .createdAt(Instant.now())
                .build();

        return applicationRepository.save(application);
    }

    // 4. 공고 신청 취소
    @Override
    public Application cancelRecruitApplication(Integer applicationId) {
        Application application = applicationRepository.findByIdAndIsDeletedFalse(applicationId)
                .orElseThrow(() -> new NotFoundException("신청 내역을 찾을 수 없습니다."));

        if (!"PENDING".equals(application.getStatus())) {
            throw new ConflictException("신청 취소는 PENDING 상태에서만 가능합니다.");
        }

        application.setIsDeleted(true);
        applicationRepository.save(application);
        return application;
    }

    // 5. 신청한 공고 목록 조회
    @Override
    public List<MyApplicationsResponseDto> getMyApplications(Integer userId, Integer cursor, Integer limit) {
        List<Application> applications = applicationRepository.findApplicationsByUserId(userId, cursor, limit);

        return applications.stream()
                .map(MyApplicationsResponseDto::from)
                .collect(Collectors.toList());
    }

    // 6. 특정 공고 상세 조회
    @Override
    public Optional<MyApplicationDetailResponseDto> getRecruitDetail(Integer userId, Integer recruitId) {
        Recruit recruit = recruitRepository.findByIdAndIsDeletedFalse(recruitId)
                .orElseThrow(() -> new NotFoundException("해당 봉사 공고를 찾을 수 없습니다."));

        Optional<Application> application = applicationRepository.findApplicationByRecruitAndUser(recruitId, userId);

        return Optional.of(MyApplicationDetailResponseDto.from(recruit, application.orElse(null)));
    }


    // 7. "좋아요"한 템플릿 목록 조회
    @Override
    public Map<String, Object> getLikedTemplates(Integer userId, Integer cursor, Integer limit) {
        List<VolunteerReaction> reactions = reactionRepository.findLikedTemplatesByUserId(userId, cursor, limit);

        List<MyLikesRecruitsResponseDto> likedTemplates = reactions.stream()
                .map(MyLikesRecruitsResponseDto::from)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("likedTemplates", likedTemplates);
        return response;
    }


    // 8. 특정 템플릿 "좋아요" 혹은 "싫어요"하기
    @Override
    public List<Integer> saveReaction(Integer userId, Integer templateId, Boolean isLike) {
        Volunteer volunteer = volunteerRepository.findByUserIdAndUserIsDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("봉사자를 찾을 수 없습니다."));

        Template template = templateRepository.findByIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> new NotFoundException("해당 템플릿이 존재하지 않습니다."));

        List<Recruit> recruits = recruitRepository.findByTemplateAndIsDeletedFalse(template);

        // 해당 템플릿을 참조하는 공고가 없으면 아무 작업도 하지 않음.
        if (recruits.isEmpty()) {
            return Collections.emptyList();
        }

        //기존에 리액션을 했었는가?
        boolean exists = reactionRepository.existsByVolunteerAndRecruitTemplate(volunteer, template);
        if (exists) { //그렇다면 소프트삭제
            reactionRepository.softDeleteByVolunteerAndTemplate(volunteer, template);
        }

        List<VolunteerReaction> newReactions = recruits.stream()
                .map(recruit -> VolunteerReaction.builder()
                        .volunteer(volunteer)
                        .recruit(recruit)
                        .isLike(isLike)
                        .isDeleted(false)
                        .createdAt(Instant.now())
                        .build())
                .toList();

        return reactionRepository.saveAll(newReactions).stream()
                .map(VolunteerReaction::getId)
                .toList();
    }


    // 9. "좋아요" 취소
    @Override
    public void deleteReactions(List<Integer> reactionIds) {
        if (reactionIds == null || reactionIds.isEmpty()) {
            throw new NotFoundException("삭제할 좋아요 ID 목록이 비어 있습니다.");
        }
        int updatedCount = reactionRepository.softDeleteByIds(reactionIds);
        if (updatedCount == 0) {
            throw new NotFoundException("해당 ID의 좋아요가 존재하지 않습니다.");
        }
    }

}
