package com.ttabong.dto.user;

import com.ttabong.entity.user.Organization;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDto {
    private Integer orgId;
    private Integer userId;
    private String businessRegNumber;
    private String orgName;
    private String representativeName;
    private String orgAddress;
    private LocalDateTime createdAt;

    public static OrganizationDto from(Organization organization) {
        if (organization == null) {
            return null;
        }
        return OrganizationDto.builder()
                .orgId(organization.getId())
                .orgName(organization.getOrgName())
                .build();
    }
}
