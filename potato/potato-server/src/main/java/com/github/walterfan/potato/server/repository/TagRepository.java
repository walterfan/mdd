package com.github.walterfan.potato.server.repository;

import com.github.walterfan.potato.server.entity.TagEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TagRepository extends PagingAndSortingRepository<TagEntity, UUID> {

    @Query(nativeQuery = true, value ="SELECT * FROM tag WHERE name IN (:names)")
    List<TagEntity> findByNames(@Param("names")Collection<String> names);

    List<TagEntity> findByName(String name);
}
