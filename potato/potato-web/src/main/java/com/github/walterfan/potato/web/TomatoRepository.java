package com.github.walterfan.potato.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @Author: Walter Fan
 * @Date: 19/7/2020, Sun
 **/

@Repository
public interface TomatoRepository extends PagingAndSortingRepository<TomatoEntity, UUID>, JpaSpecificationExecutor<TomatoEntity> {
    Page<TomatoEntity> findByPotatoId(UUID potatoId, Pageable pageable);

    Page<TomatoEntity> findByPotatoId(UUID potatoId, Specification<TomatoEntity> spec, Pageable pageable);

    TomatoEntity findByPotatoIdAndName(UUID potatoId, String name);

}



