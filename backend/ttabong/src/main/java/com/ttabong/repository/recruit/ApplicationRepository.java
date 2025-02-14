package com.ttabong.repository.recruit;

import com.ttabong.entity.recruit.Application;
import com.ttabong.entity.user.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    @Query("SELECT a FROM Application a " +
            "JOIN FETCH a.volunteer v " +
            "JOIN FETCH v.user u " +
            "WHERE a.recruit.id = :recruitId")
    List<Application> findByRecruitIdWithUser(@Param("recruitId") Integer recruitId);

    @Transactional
    @Modifying
    @Query("UPDATE Application a SET a.status = :status WHERE a.id = :applicationId")
    void updateApplicationStatus(@Param("applicationId") Integer applicationId, @Param("status") String status);

    // for VolRecruit -------------------------------------------
    // 사용자가 신청한 모집 공고 목록 조회
//    @Query("SELECT a FROM Application a WHERE a.volunteer.user.id = :userId AND a.id > :cursor AND a.isDeleted = FALSE ORDER BY a.createdAt DESC")
//    List<Application> findApplicationsByUserId(@Param("userId") Integer userId, @Param("cursor") Integer cursor, @Param("limit") Integer limit);

    @Query("SELECT a FROM Application a WHERE a.volunteer.id = (SELECT v.id FROM Volunteer v WHERE v.user.id = :userId) AND a.id > :cursor AND a.isDeleted = FALSE ORDER BY a.createdAt DESC Limit :limit")
    List<Application> findApplicationsByUserId(@Param("userId") Integer userId,@Param("cursor") Integer cursor,@Param("limit") Integer limit);

    // 해당 봉사자가 해당 공고를 신청했는지 확인
    @Query("SELECT a FROM Application a WHERE a.recruit.id = :recruitId AND a.volunteer.user.id = :userId AND a.isDeleted = FALSE")
    Optional<Application> findApplicationByRecruitAndUser(@Param("recruitId") Integer recruitId, @Param("userId") Integer userId);

}
