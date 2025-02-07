package com.ttabong.repository.recruit;

import com.ttabong.entity.recruit.Recruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface RecruitRepository extends JpaRepository<Recruit, Integer> {

    List<Recruit> findByTemplateId(Integer templateId);

    @Query("SELECT r FROM Recruit r WHERE (:cursor IS NULL OR r.id < :cursor) ORDER BY r.id DESC LIMIT :limit")
    List<Recruit> findAvailableRecruits(@Param("cursor") Integer cursor, @Param("limit") Integer limit);

    @Modifying
    @Query("UPDATE Recruit r SET r.isDeleted = true WHERE r.id IN :deleteIds")
    void markAsDeleted(@Param("deleteIds") List<Integer> deleteIds);

    @Modifying
    @Query("UPDATE Recruit r " +
            "SET r.deadline = :deadline, " +
            "r.activityDate = :activityDate, " +
            "r.activityStart = :activityStart, " +
            "r.activityEnd = :activityEnd, " +
            "r.maxVolunteer = :maxVolunteer, " +
            "r.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE r.id = :recruitId")
    void updateRecruit(
            @Param("recruitId") Integer recruitId,
            @Param("deadline") Instant deadline,
            @Param("activityDate") Date activityDate,
            @Param("activityStart") Double activityStart,
            @Param("activityEnd") Double activityEnd,
            @Param("maxVolunteer") Integer maxVolunteer
    );

}
