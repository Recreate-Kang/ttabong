package com.ttabong.repository.sns;

import com.ttabong.entity.sns.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("""
        SELECT r FROM Review r
        LEFT JOIN FETCH r.writer w
        LEFT JOIN FETCH r.org o
        LEFT JOIN FETCH r.recruit rec
        LEFT JOIN FETCH rec.template t
        LEFT JOIN FETCH t.group g
        WHERE (:cursor IS NULL OR r.id < :cursor)
        AND r.isDeleted = false
        ORDER BY r.id DESC
    """)
    List<Review> findAllReviews(@Param("cursor") Integer cursor, Pageable pageable);

//    @Query("""
//        SELECT r FROM Review r
//        WHERE r.org.id = r.writer.id
//        AND r.recruit.id = :recruitId
//        ORDER BY r.createdAt DESC
//        LIMIT 1
//""")
    @Query("""
        SELECT r FROM Review r
        WHERE r.org.id = r.writer.id
        AND r.recruit.id = :recruitId
        ORDER BY r.createdAt DESC
    """)
    List<Review> findByOrgWriterAndRecruit(@Param("recruitId") Integer recruitId);

//    @Query("""
//    SELECT r FROM Review r
//    WHERE r.org.id = r.writer.id  -- ✅ writer_id == org_id 조건 추가
//    AND r.recruit.id = :recruitId  -- ✅ recruit_id가 같은 리뷰 찾기
//    ORDER BY r.createdAt DESC
//    LIMIT 1
//""")
//    Optional<Review> findTopByOrgWriterAndRecruit(
//            @Param("recruitId") Integer recruitId
//    );


    @Query("""
        SELECT r FROM Review r
        LEFT JOIN FETCH r.org o
        LEFT JOIN FETCH r.recruit rec
        LEFT JOIN FETCH rec.template t
        LEFT JOIN FETCH t.group g
        LEFT JOIN FETCH r.reviewImages ri
        WHERE r.isDeleted = false
        AND r.id IN (SELECT r2.id FROM Review r2 WHERE r2.writer.id = :userId)
        ORDER BY r.id DESC
    """)
    List<Review> findMyReviews(@Param("userId") Integer userId, Pageable pageable);



    @Query("""
        SELECT r FROM Review r
        LEFT JOIN FETCH r.writer w
        LEFT JOIN FETCH r.org o
        LEFT JOIN FETCH r.recruit rec
        LEFT JOIN FETCH rec.template t
        LEFT JOIN FETCH t.group g
        WHERE rec.id = :recruitId
        AND r.isDeleted = false
        ORDER BY r.createdAt DESC
    """)
    List<Review> findByRecruitId(@Param("recruitId") Integer recruitId);


}
