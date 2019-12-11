package com.github.walterfan.potato.server;


import com.github.walterfan.potato.client.PotatoSchedulerClient;
import com.github.walterfan.potato.common.dto.PotatoDTO;
import com.github.walterfan.potato.common.dto.RemindEmailRequest;
import com.github.walterfan.potato.common.dto.RemindEmailResponse;
import com.github.walterfan.potato.common.dto.SearchCriteria;
import com.github.walterfan.potato.common.util.TemplateHelper;
import com.github.walterfan.potato.server.entity.PotatoEntity;
import com.github.walterfan.potato.server.entity.TagEntity;
import com.github.walterfan.potato.server.repository.PotatoRepository;
import com.github.walterfan.potato.server.repository.PotatoSpecification;
import com.github.walterfan.potato.server.repository.TagRepository;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;


import java.time.Instant;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import java.util.HashSet;
import java.util.List;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author: Walter Fan
 **/
@Service
@Slf4j
public class PotatoServiceImpl implements PotatoService {

    @Value("${potato.remind.email}")
    private String remindEmail;

    @Autowired
    private PotatoRepository potatoRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PotatoSchedulerClient potatoSchedulerClient;

    @Autowired
    private TemplateHelper templateHelper;

    @Override
    public PotatoDTO create(PotatoDTO potatoRequest) {
        PotatoEntity potato = potatoDto2Entity(potatoRequest, null);
        PotatoEntity savedPotato =  potatoRepository.save(potato);
        scheduleRemindEmails(potatoRequest);

        return this.potatoEntity2Dto(savedPotato);
    }


    @HystrixCommand(fallbackMethod = "recordRemindEmails")
    private void scheduleRemindEmails(PotatoDTO potatoRequest) {
        String emailContent = potatoRequest.getDescription();
        //schedule reminds
        scheduleRemindEmail(potatoRequest, "To start: " + potatoRequest.getName(), emailContent);
        scheduleRemindEmail(potatoRequest, "To finish: " + potatoRequest.getName(), emailContent);
    }

    private void scheduleRemindEmail(PotatoDTO potatoRequest, String subject, String emailContent) {
        String emailBox = potatoRequest.getEmail();
        if(StringUtils.isBlank(emailBox)) {
            emailBox = this.remindEmail;
        }
        
        RemindEmailRequest remindEmailRequest = RemindEmailRequest.builder()
                .email(emailBox)
                .subject(subject)
                .body(emailContent)
                .dateTime(potatoRequest.getScheduleTime())
                .build();
        try {
            ResponseEntity<RemindEmailResponse> responseEntity = potatoSchedulerClient.scheduleRemindEmail(remindEmailRequest);
            log.info("respEntity for remind : {}", responseEntity.getStatusCode());
        } catch(Exception e) {
            log.error("scheduleRemindEmail error", e);
        }
    }

    @Override
    public PotatoDTO retrieve(UUID id) {
        log.info("retrievePotato: {}", id);
        Optional<PotatoEntity> optPotato = potatoRepository.findById(id);
        if(!optPotato.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Potato Not Found of " + id.toString());
        }
        return optPotato.map(x -> potatoEntity2Dto(x)).orElse(null);
    }

    @Override
    public PotatoDTO update(PotatoDTO potatoDto) {
        Preconditions.checkArgument(potatoDto.getId() != null);
        Optional<PotatoEntity> optEntity = potatoRepository.findById(potatoDto.getId());
        PotatoEntity newEntity = potatoDto2Entity(potatoDto, optEntity.orElse(null));
        PotatoEntity savedPotato =  potatoRepository.save(newEntity);

        PotatoDTO dto = this.potatoEntity2Dto(savedPotato);

        scheduleRemindEmails(dto);

        return dto;
    }

    @Override
    public void delete(UUID id) {
        this.potatoRepository.deleteById(id);
    }

    @Override
    public List<PotatoDTO> search(UUID userId, String keyword, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "priority");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        PotatoSpecification spec1 =
                new PotatoSpecification(new SearchCriteria("name", ":", keyword));
        PotatoSpecification spec2 =
                new PotatoSpecification(new SearchCriteria("description", ":", keyword));

        Page<PotatoEntity> pageResult = potatoRepository.findByUserId(userId, Specifications.where(spec1).or(spec2), pageRequest);

        List<PotatoEntity> potatoList = pageResult.getContent();

        List<PotatoDTO> resultList = potatoList.stream().map(x -> potatoEntity2Dto(x)).collect(Collectors.toList());

        return resultList;
    }

    @Override
    public List<PotatoDTO> list(UUID userId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "priority");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        List<PotatoEntity> potatoList = Lists.newArrayList(potatoRepository.findByUserId(userId, pageRequest));

        List<PotatoDTO> resultList = potatoList.stream().map(x -> potatoEntity2Dto(x)).collect(Collectors.toList());

        return resultList;
    }

    @Override
    public void startPotato(UUID potatoId) {
        PotatoDTO potatoDTO = this.retrieve(potatoId);
        potatoDTO.setStartTime(ZonedDateTime.now());
    }

    @Override
    public void stopPotato(UUID potatoId) {
        PotatoDTO potatoDTO = this.retrieve(potatoId);
        potatoDTO.setEndTime(ZonedDateTime.now());
    }



    private PotatoEntity potatoDto2Entity(PotatoDTO potatoRequest, PotatoEntity potatoEntity) {
        if(potatoEntity == null) {
            potatoEntity = new PotatoEntity();
        }

        BeanUtils.copyProperties(potatoRequest, potatoEntity);
        potatoEntity.setDeadline(toDate(potatoRequest.getDeadline()));
        potatoEntity.setScheduleTime(toDate(potatoRequest.getScheduleTime()));
        potatoEntity.setStartTime(toDate(potatoRequest.getStartTime()));
        potatoEntity.setEndTime(toDate(potatoRequest.getEndTime()));

        String tags = potatoRequest.getTags();
        if (StringUtils.isBlank(tags)) {
            potatoEntity.getTags().clear();
        } else {
            addTags(potatoEntity, tags);
        }

        log.info("potatoDto2Entity {} --> {}", potatoRequest, potatoEntity);
        return potatoEntity;
    }

    private void addTags(PotatoEntity potatoEntity, String tags) {
        Set<String> tagSet= new HashSet<>(Arrays.asList(tags.split(",")));

        List<TagEntity> tagEntities = findTags(tagSet);

        if(!CollectionUtils.isEmpty(tagEntities)) {

            for(TagEntity entity: tagEntities) {
                potatoEntity.getTags().add(entity);
                tagSet.remove(entity.getName());
            }

        }

        for(String tagName: tagSet) {
            TagEntity tag = new TagEntity();
            tag.setName(tagName);
            potatoEntity.getTags().add(tag);
        }
    }


    private List<TagEntity> findTags(Collection<String> tagNames) {
        return tagRepository.findByNames(tagNames);
    }

    private PotatoDTO potatoEntity2Dto(PotatoEntity potatoEntity) {
        PotatoDTO potatoDto = new PotatoDTO();

        potatoDto.setId(potatoEntity.getId());
        BeanUtils.copyProperties(potatoEntity, potatoDto);

        potatoDto.setScheduleTime(toZonedDateTime(potatoEntity.getScheduleTime()));
        potatoDto.setDeadline(toZonedDateTime(potatoEntity.getDeadline()));
        potatoDto.setStartTime(toZonedDateTime(potatoEntity.getStartTime()));
        potatoDto.setEndTime(toZonedDateTime(potatoEntity.getEndTime()));

        if (!CollectionUtils.isEmpty(potatoEntity.getTags())) {
            potatoDto.setTags(potatoEntity.getTags().stream().map(x -> x.getName()).collect(Collectors.joining(",")));
        }

        log.info("G: {} --> {}", potatoEntity, potatoDto);
        return potatoDto;
    }

    private Date toDate(ZonedDateTime zonedDateTime) {
        if(zonedDateTime == null) {
            return null;
        }
        return new Date(zonedDateTime.toInstant().toEpochMilli());
    }

    private ZonedDateTime toZonedDateTime(Date date) {
        if(date == null) {
            return null;
        }

        Instant instant = Instant.ofEpochMilli(date.getTime());
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
