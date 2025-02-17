package com.ttabong.repositoryjpa.recruit;

import com.ttabong.entity.recruit.Template;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepositoryJpa extends JpaRepository<Template, Integer> {

    @EntityGraph(attributePaths = {"org"})
    Optional<Template> findTemplateById(Integer id);

    @EntityGraph
    List<Template> findByIdInAndIsDeletedFalse(List<Integer> ids);

    @EntityGraph(attributePaths = {"group"})
    List<Template> findByGroupIdAndIsDeletedFalse(Integer id, Pageable pageable);

    Optional<Template> findByIdAndIsDeletedFalse(Integer templateId);
}
