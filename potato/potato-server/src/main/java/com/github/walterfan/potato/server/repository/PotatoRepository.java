package com.github.walterfan.potato.server.repository;

import com.github.walterfan.potato.server.entity.PotatoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface PotatoRepository extends PagingAndSortingRepository<PotatoEntity, UUID>, JpaSpecificationExecutor<PotatoEntity> {
    Page<PotatoEntity> findByUserId(UUID userId, Pageable pageable);

    Page<PotatoEntity> findByUserId(UUID userId, Specification<PotatoEntity> spec, Pageable pageable);

    PotatoEntity findByUserIdAndName(UUID userId, String name);

}

