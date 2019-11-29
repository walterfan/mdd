package com.github.walterfan.potato.metrics.repository;

import com.github.walterfan.potato.metrics.entity.MetricsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


/**
 * Created by yafan on 26/11/2017.
 */
@Repository
public interface PotatoMetricsRepository extends PagingAndSortingRepository<MetricsEntity, UUID>, JpaSpecificationExecutor<MetricsEntity> {


}

