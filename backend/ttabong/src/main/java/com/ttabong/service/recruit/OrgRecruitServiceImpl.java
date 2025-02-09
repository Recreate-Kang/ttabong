package com.ttabong.service.recruit;

import com.ttabong.dto.recruit.requestDto.org.*;
import com.ttabong.dto.recruit.responseDto.org.*;
import com.ttabong.dto.recruit.responseDto.org.ReadAvailableRecruitsResponseDto.TemplateDetail;
import com.ttabong.dto.recruit.responseDto.org.ReadMyRecruitsResponseDto.RecruitDetail;
import com.ttabong.entity.recruit.*;
import com.ttabong.entity.user.Organization;
import com.ttabong.repository.recruit.*;
import com.ttabong.repository.user.OrganizationRepository;
import com.ttabong.repository.user.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrgRecruitServiceImpl implements OrgRecruitService {

    private final RecruitRepository recruitRepository;
    private final TemplateRepository templateRepository;
    private final TemplateGroupRepository templateGroupRepository;
    private final OrganizationRepository organizationRepository;
    private final CategoryRepository categoryRepository;
    private final TemplateImageRepository templateImageRepository;
    private final ApplicationRepository applicationRepository;
    private final VolunteerRepository volunteerRepository;

    // TODO: 마지막 공고까지 다 로드했다면? & db에서 정보 누락된게 있다면? , 삭제여부 확인, 마감인건 빼고 가져오기
    @Override
    @Transactional(readOnly = true)
    public ReadAvailableRecruitsResponseDto readAvailableRecruits(Integer cursor, Integer limit) {

        List<Template> templates = templateRepository.findAvailableTemplates(cursor, limit);

        List<TemplateDetail> templateDetails = templates.stream().map(template -> {
            TemplateGroup templateGroup = template.getGroup();
            ReadAvailableRecruitsResponseDto.Group group = Optional.ofNullable(templateGroup)
                    .map(g -> ReadAvailableRecruitsResponseDto.Group.builder()
                            .groupId(g.getId())
                            .groupName(g.getGroupName())
                            .build())
                    .orElse(null);

            List<Recruit> recruitEntities = recruitRepository.findByTemplateId(template.getId());
            List<ReadAvailableRecruitsResponseDto.Recruit> recruits = recruitEntities.stream()
                    .map(recruit -> ReadAvailableRecruitsResponseDto.Recruit.builder()
                            .recruitId(recruit.getId())
                            .deadline(recruit.getDeadline() != null ?
                                    recruit.getDeadline().atZone(ZoneId.systemDefault()).toLocalDateTime()
                                    : LocalDateTime.now())
                            .activityDate(recruit.getActivityDate() != null
                                    ? recruit.getActivityDate()
                                    : new Date())
                            .activityStart(recruit.getActivityStart() != null ? recruit.getActivityStart() : BigDecimal.ZERO)
                            .activityEnd(recruit.getActivityEnd() != null ? recruit.getActivityEnd() : BigDecimal.ZERO)
                            .maxVolunteer(recruit.getMaxVolunteer())
                            .participateVolCount(recruit.getParticipateVolCount())
                            .status(recruit.getStatus())
                            .updatedAt(recruit.getUpdatedAt() != null ?
                                    recruit.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                                    : LocalDateTime.now())
                            .createdAt(recruit.getCreatedAt() != null ?
                                    recruit.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                                    : LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());

            return TemplateDetail.builder()
                    .template(ReadAvailableRecruitsResponseDto.Template.builder()
                            .templateId(template.getId())
                            .categoryId(template.getCategory() != null ? template.getCategory().getId() : null)
                            .title(template.getTitle())
                            .activityLocation(template.getActivityLocation())
                            .status(template.getStatus())
                            .imageId(template.getImageId())
                            .contactName(template.getContactName())
                            .contactPhone(template.getContactPhone())
                            .description(template.getDescription())
                            .createdAt(template.getCreatedAt() != null ?
                                    template.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                                    : LocalDateTime.now())
                            .build())
                    .group(group)
                    .recruits(recruits)
                    .build();
        }).collect(Collectors.toList());

        return ReadAvailableRecruitsResponseDto.builder()
                .templates(templateDetails)
                .build();

    }

    // TODO: 마지막 공고까지 다 로드했다면? & db에서 정보 누락된게 있다면?, 삭제여부 확인
    @Override
    @Transactional(readOnly = true)
    public ReadMyRecruitsResponseDto readMyRecruits(Integer cursor, Integer limit) {

        List<Recruit> recruits = recruitRepository.findAvailableRecruits(cursor, limit);

        List<RecruitDetail> recruitDetails = recruits.stream().map(recruit -> {
            Template template = recruit.getTemplate();
            TemplateGroup templateGroup = template.getGroup();
            ReadMyRecruitsResponseDto.Group group = Optional.ofNullable(templateGroup)
                    .map(g -> ReadMyRecruitsResponseDto.Group.builder()
                            .groupId(g.getId())
                            .groupName(g.getGroupName())
                            .build())
                    .orElse(null);
            ReadMyRecruitsResponseDto.Template dtoTemplate = ReadMyRecruitsResponseDto.Template.builder()
                    .templateId(template.getId())
                    .title(template.getTitle())
                    .build();
            ReadMyRecruitsResponseDto.Recruit dtoRecruit = ReadMyRecruitsResponseDto.Recruit.builder()
                    .recruitId(recruit.getId())
                    .status(recruit.getStatus())
                    .maxVolunteer(recruit.getMaxVolunteer())
                    .participateVolCount(recruit.getParticipateVolCount())
                    .activityDate(recruit.getActivityDate() != null ? recruit.getActivityDate() : new Date())
                    .activityStart(recruit.getActivityStart() != null ? recruit.getActivityStart() : BigDecimal.ZERO)
                    .activityEnd(recruit.getActivityEnd() != null ? recruit.getActivityEnd() : BigDecimal.ZERO)
                    .deadline(recruit.getDeadline() != null ?
                            recruit.getDeadline().atZone(ZoneId.systemDefault()).toLocalDateTime()
                            : LocalDateTime.now())
                    .createdAt(recruit.getCreatedAt() != null ?
                            recruit.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                            : LocalDateTime.now())
                    .build();

            return RecruitDetail.builder()
                    .group(group)
                    .template(dtoTemplate)
                    .recruit(dtoRecruit)
                    .build();
        }).collect(Collectors.toList());

        return ReadMyRecruitsResponseDto.builder()
                .recruits(recruitDetails)
                .build();
    }

    // TODO: 이미 삭제된 공고는 어떻게 처리? 삭제 실패시 처리
    @Override
    public DeleteRecruitsResponseDto deleteRecruits(DeleteRecruitsRequestDto deleteRecruitDto) {

        List<Integer> recruitIds = deleteRecruitDto.getDeletedRecruits();

        recruitRepository.markAsDeleted(recruitIds);

        return DeleteRecruitsResponseDto.builder()
                .message("공고 삭제 완료")
                .deletedRecruits(recruitIds)
                .build();

    }

    @Override
    public UpdateRecruitsResponseDto updateRecruit(Integer recruitId, UpdateRecruitsRequestDto requestDto) {

        Instant deadlineInstant = requestDto.getDeadline() != null
                ? requestDto.getDeadline().atZone(ZoneId.systemDefault()).toInstant()
                : Instant.now(); // null일 경우 현재 시간 설정

        Date activityDate = requestDto.getActivityDate() != null
                ? new java.sql.Date(requestDto.getActivityDate().getTime())
                : new java.sql.Date(System.currentTimeMillis());

        recruitRepository.updateRecruit(
                recruitId,
                deadlineInstant,
                activityDate,
                requestDto.getActivityStart() != null ? requestDto.getActivityStart() : BigDecimal.ZERO,
                requestDto.getActivityEnd() != null ? requestDto.getActivityEnd() : BigDecimal.ZERO,
                requestDto.getMaxVolunteer()
        );

        return UpdateRecruitsResponseDto.builder()
                .message("공고 수정 완료")
                .recruitId(recruitId)
                .build();

    }

    // TODO: db에 있는지 보고, 마감으로 수정하기
    @Override
    public CloseRecruitResponseDto closeRecruit(CloseRecruitRequestDto closeRecruitDto) {

        Integer recruitId = closeRecruitDto.getRecruitId();

        recruitRepository.closeRecruit(recruitId);

        return CloseRecruitResponseDto.builder()
                .message("공고 마감 완료")
                .recruitId(recruitId)
                .build();

    }

    @Override
    public UpdateGroupResponseDto updateGroup(UpdateGroupRequestDto updateGroupDto) {

        // TODO: 토큰 인증 할거지만, 일단 기관까지 그냥 체크해주자 +그룹id로 하자
        Organization org = organizationRepository.findById(updateGroupDto.getOrgId())
                .orElseThrow(() -> new IllegalArgumentException("해당 기관 없음"));

        templateGroupRepository.updateGroup(updateGroupDto.getGroupId(), org, updateGroupDto.getGroupName());

        return UpdateGroupResponseDto.builder()
                .message("수정 성공")
                .groupId(updateGroupDto.getGroupId())
                .orgId(updateGroupDto.getOrgId())
                .build();

    }

    @Override
    public UpdateTemplateResponse updateTemplate(UpdateTemplateRequestDto updateTemplateDto) {

        Organization org = organizationRepository.findById(updateTemplateDto.getOrgId())
                .orElseThrow(() -> new IllegalArgumentException("해당 기관 없음"));

        templateRepository.updateTemplate(updateTemplateDto.getTemplateId(), org, updateTemplateDto.getTitle(),
                updateTemplateDto.getDescription(), updateTemplateDto.getActivityLocation(),
                updateTemplateDto.getContactName(), updateTemplateDto.getContactPhone());

        return UpdateTemplateResponse.builder()
                .message("템플릿 수정 성공")
                .templateId(updateTemplateDto.getTemplateId())
                .orgId(updateTemplateDto.getOrgId())
                .build();

    }

    @Override
    @Transactional
    public DeleteTemplatesResponseDto deleteTemplates(DeleteTemplatesRequestDto deleteTemplatesDto) {

        List<Integer> deleteTemplateIds = deleteTemplatesDto.getDeletedTemplates();

        templateRepository.deleteTemplates(deleteTemplateIds);

        return DeleteTemplatesResponseDto.builder()
                .message("템플릿 삭제 성공")
                .deletedTemplates(deleteTemplateIds)  // 삭제된 템플릿 ID 리스트 전달
                .build();

    }

    @Override
    public DeleteGroupResponseDto deleteGroup(DeleteGroupDto deleteGroupDto) {

        Integer groupId = deleteGroupDto.getGroupId();
        Integer orgId = deleteGroupDto.getOrgId();

        templateGroupRepository.deleteGroupByIdAndOrg(groupId, orgId);

        return DeleteGroupResponseDto.builder()
                .message("삭제 성공")
                .groupId(groupId)
                .orgId(orgId)
                .build();

    }

    @Override
    public ReadTemplatesResponseDto readTemplates(Integer cursor, Integer limit) {

        // Pageable 생성: cursor가 페이지 번호(0부터 시작), limit가 한 페이지에 보여줄 데이터 수
        Pageable pageable = PageRequest.of(cursor, limit);

        List<TemplateGroup> groups = templateGroupRepository.findGroups(pageable);

        List<ReadTemplatesResponseDto.GroupDto> groupDtos = groups.stream()
                .map(group -> {
                    // 그룹 dto 생성
                    ReadTemplatesResponseDto.GroupDto groupDto = ReadTemplatesResponseDto.GroupDto.builder()
                            .groupId(group.getId())
                            .groupName(group.getGroupName())
                            .templates(
                                    // 그룹에 속한 템플릿 목록 조회
                                    templateRepository.findTemplatesByGroupId(group.getId()).stream()
                                            .map(template -> ReadTemplatesResponseDto.TemplateDto.builder()
                                                    .templateId(template.getId())
                                                    .orgId(template.getOrg().getId())
                                                    .categoryId(template.getCategory() != null ? template.getCategory().getId() : null)
                                                    .title(template.getTitle())
                                                    .activityLocation(template.getActivityLocation())
                                                    .status(template.getStatus())
                                                    .imageId(template.getImageId())
                                                    .contactName(template.getContactName())
                                                    .contactPhone(template.getContactPhone())
                                                    .description(template.getDescription())
                                                    .createdAt(template.getCreatedAt() != null
                                                            ? LocalDateTime.ofInstant(template.getCreatedAt(), ZoneId.systemDefault())
                                                            : LocalDateTime.now())
                                                    .build()
                                            ).collect(Collectors.toList())
                            )
                            .build();

                    return groupDto;

                })
                .collect(Collectors.toList());

        return ReadTemplatesResponseDto.builder()
                .groups(groupDtos)
                .build();

    }

    // TODO: 이미지 저장하기 (지금은 임시로 Template_image 테이블 하나 더 만들어서 사용중)
    @Override
    public CreateTemplateResponseDto createTemplate(CreateTemplateRequestDto createTemplateDto) {

        Template template = Template.builder()
                .group(templateGroupRepository.findById(createTemplateDto.getGroupId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 그룹 없음")))
                .org(organizationRepository.findById(createTemplateDto.getOrgId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 기관 없음")))
                .category(categoryRepository.findById(createTemplateDto.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 카테고리 없음")))
                .title(createTemplateDto.getTitle())
                .activityLocation(createTemplateDto.getActivityLocation())
                .status(createTemplateDto.getStatus())
                .contactName(createTemplateDto.getContactName())
                .contactPhone(createTemplateDto.getContactPhone())
                .description(createTemplateDto.getDescription())
                .isDeleted(false)
                .createdAt(Instant.now())
                .build();

        Template savedTemplate = templateRepository.save(template);

        // 이미지 처리: 받은 이미지 리스트에서 각 이미지를 TemplateImage 테이블에 저장
        if (createTemplateDto.getImages() != null && !createTemplateDto.getImages().isEmpty()) {
            List<TemplateImage> templateImages = createTemplateDto.getImages().stream()
                    .map(imageUrl -> TemplateImage.builder()
                            .template(savedTemplate)
                            .imageUrl(imageUrl)
                            .createdAt(Instant.now())
                            .build())
                    .collect(Collectors.toList());

            templateImageRepository.saveAll(templateImages);
        }
        
        return CreateTemplateResponseDto.builder()
                .message("템플릿 생성 성공")
                .templateId(savedTemplate.getId())
                .build();

    }

    @Override
    public CreateGroupResponseDto createGroup(CreateGroupRequestDto createGroupDto) {

        Organization org = organizationRepository.findById(createGroupDto.getOrgId())
                .orElseThrow(() -> new IllegalArgumentException("기관 없음"));

        TemplateGroup newGroup = TemplateGroup.builder()
                .org(org)
                .groupName(createGroupDto.getGroupName() != null ? createGroupDto.getGroupName() : "봉사")
                .isDeleted(false)
                .build();

        TemplateGroup savedGroup = templateGroupRepository.save(newGroup);

        return CreateGroupResponseDto.builder()
                .message("그룹 생성 성공")
                .groupId(savedGroup.getId())
                .build();

    }

    @Override
    public CreateRecruitResponseDto createRecruit(CreateRecruitRequestDto createRecruitDto) {

        Instant deadlineInstant = createRecruitDto.getDeadline() != null
                ? createRecruitDto.getDeadline().atZone(ZoneId.systemDefault()).toInstant()
                : Instant.now();

        Template template = templateRepository.findById(createRecruitDto.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("해당 템플릿이 존재하지 않습니다."));

        Recruit recruit = Recruit.builder()
                .template(template)
                .deadline(deadlineInstant)
                .activityDate(createRecruitDto.getActivityDate() != null ? createRecruitDto.getActivityDate() : new Date())
                .activityStart(createRecruitDto.getActivityStart() != null ? createRecruitDto.getActivityStart() : BigDecimal.ZERO)
                .activityEnd(createRecruitDto.getActivityEnd() != null ? createRecruitDto.getActivityEnd() : BigDecimal.ZERO)
                .maxVolunteer(createRecruitDto.getMaxVolunteer() != null ? createRecruitDto.getMaxVolunteer() : 0)
                .status("RECRUITING")
                .isDeleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        recruit = recruitRepository.save(recruit);

        return CreateRecruitResponseDto.builder()
                .message("공고 생성 완료")
                .recruitId(recruit.getId())
                .build();

    }

    @Override
    public ReadRecruitResponseDto readRecruit(Integer recruitId) {

        Recruit recruit = recruitRepository.findById(recruitId)
                .orElseThrow(() -> new RuntimeException("해당 공고가 없습니다"));

        LocalDateTime deadlineLocalDateTime = recruit.getDeadline() != null
                ? LocalDateTime.ofInstant(recruit.getDeadline(), ZoneId.systemDefault())
                : null;

        LocalDateTime updatedAtLocalDateTime = recruit.getUpdatedAt() != null
                ? LocalDateTime.ofInstant(recruit.getUpdatedAt(), ZoneId.systemDefault())
                : LocalDateTime.now();

        LocalDateTime createdAtLocalDateTime = recruit.getCreatedAt() != null
                ? LocalDateTime.ofInstant(recruit.getCreatedAt(), ZoneId.systemDefault())
                : LocalDateTime.now();

        Date activityDate = recruit.getActivityDate() != null ? recruit.getActivityDate() : new Date();

        ReadRecruitResponseDto.Recruit recruitDto = ReadRecruitResponseDto.Recruit.builder()
                .recruitId(recruit.getId())
                .deadline(deadlineLocalDateTime)
                .activityDate(activityDate)
                .activityStart(recruit.getActivityStart() != null ? recruit.getActivityStart() : BigDecimal.ZERO)
                .activityEnd(recruit.getActivityEnd() != null ? recruit.getActivityEnd() : BigDecimal.ZERO)
                .maxVolunteer(recruit.getMaxVolunteer())
                .participateVolCount(recruit.getParticipateVolCount())
                .status(recruit.getStatus())
                .updatedAt(updatedAtLocalDateTime)
                .createdAt(createdAtLocalDateTime)
                .build();

        ReadRecruitResponseDto.Group groupDto = new ReadRecruitResponseDto.Group(
                recruit.getTemplate().getGroup().getId(),
                recruit.getTemplate().getGroup().getGroupName()
        );

        LocalDateTime templateCreatedAt = recruit.getTemplate().getCreatedAt() != null
                ? LocalDateTime.ofInstant(recruit.getTemplate().getCreatedAt(), ZoneId.systemDefault())
                : LocalDateTime.now();

        ReadRecruitResponseDto.Template templateDto = ReadRecruitResponseDto.Template.builder()
                .templateId(recruit.getTemplate().getId())
                .categoryId(recruit.getTemplate().getCategory() != null ? recruit.getTemplate().getCategory().getId() : null)
                .title(recruit.getTemplate().getTitle())
                .activityLocation(recruit.getTemplate().getActivityLocation())
                .status(recruit.getTemplate().getStatus())
                .imageId(recruit.getTemplate().getImageId())
                .contactName(recruit.getTemplate().getContactName())
                .contactPhone(recruit.getTemplate().getContactPhone())
                .description(recruit.getTemplate().getDescription())
                .createdAt(templateCreatedAt)
                .build();

        ReadRecruitResponseDto.Organization orgDto = new ReadRecruitResponseDto.Organization(
                recruit.getTemplate().getOrg().getId(),
                recruit.getTemplate().getOrg().getOrgName()
        );

        return ReadRecruitResponseDto.builder()
                .group(groupDto)
                .template(templateDto)
                .recruit(recruitDto)
                .organization(orgDto)
                .build();

    }

    @Override
    public ReadApplicationsResponseDto readApplications(Integer recruitId) {

        List<Application> applications = applicationRepository.findByRecruitIdWithUser(recruitId);

        List<ReadApplicationsResponseDto.ApplicationDetail> applicationDetails = applications.stream()
                .map(application -> ReadApplicationsResponseDto.ApplicationDetail.builder()
                        .user(ReadApplicationsResponseDto.User.builder()
                                .userId(application.getVolunteer().getUser().getId())
                                .email(application.getVolunteer().getUser().getEmail())
                                .name(application.getVolunteer().getUser().getName())
                                .profileImage(application.getVolunteer().getUser().getProfileImage())
                                .build())
                        .volunteer(ReadApplicationsResponseDto.Volunteer.builder()
                                .volunteerId(application.getVolunteer().getId())
                                .recommendedCount(application.getVolunteer().getRecommendedCount())
                                .totalVolunteerHours(
                                        application.getVolunteer().getUser().getTotalVolunteerHours() != null
                                                ? application.getVolunteer().getUser().getTotalVolunteerHours().intValue()
                                                : 0
                                )
                                .build())
                        .application(ReadApplicationsResponseDto.Application.builder()
                                .applicationId(application.getId())
                                .recruitId(application.getRecruit().getId())
                                .status(application.getStatus())
                                .createdAt(application.getCreatedAt() != null
                                        ? LocalDateTime.ofInstant(application.getCreatedAt(), ZoneId.systemDefault())
                                        : LocalDateTime.now())
                                .build())
                        .build())
                .collect(Collectors.toList());

        return ReadApplicationsResponseDto.builder()
                .recruitId(recruitId)
                .applications(applicationDetails)
                .build();

    }

    @Override
    @Transactional
    public UpdateApplicationsResponseDto updateStatuses(UpdateApplicationsRequestDto updateApplicationDto) {

        Integer applicationId = updateApplicationDto.getApplicationId();
        Integer recruitId = updateApplicationDto.getRecruitId();
        Boolean accept = updateApplicationDto.getAccept();

        String status = accept ? "APPROVED" : "REJECTED";

        applicationRepository.updateApplicationStatus(applicationId, status);

        return UpdateApplicationsResponseDto.builder()
                .message("신청 상태 변경 완료")
                .application(UpdateApplicationsResponseDto.Application.builder()
                        .applicationId(applicationId)
                        .recruitId(recruitId)
                        .status(status)
                        .createdAt(LocalDateTime.now())
                        .build())
                .build();

    }

    @Override
    @Transactional
    public List<EvaluateApplicationsResponseDto> evaluateApplicants(Integer recruitId, List<EvaluateApplicationsRequestDto> evaluateApplicationDtoList) {

        return evaluateApplicationDtoList.stream().map(dto -> {
            Integer volunteerId = dto.getVolunteerId();
            String recommendationStatus = dto.getRecommendationStatus();

            if ("RECOMMEND".equalsIgnoreCase(recommendationStatus)) {
                volunteerRepository.incrementRecommendation(volunteerId);
            } else if ("NOTRECOMMEND".equalsIgnoreCase(recommendationStatus)) {
                volunteerRepository.incrementNotRecommendation(volunteerId);
            }

            return EvaluateApplicationsResponseDto.builder()
                    .volunteerId(volunteerId)
                    .recommendationStatus(recommendationStatus)
                    .build();
        }).collect(Collectors.toList());

    }

}
