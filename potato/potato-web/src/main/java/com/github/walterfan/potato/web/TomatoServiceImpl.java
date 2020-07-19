package com.github.walterfan.potato.web;

import com.github.walterfan.potato.common.dto.TomatoDTO;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author: Walter Fan
 * @Date: 19/7/2020, Sun
 **/
@Service
@Slf4j
public class TomatoServiceImpl implements TomatoService {

    @Autowired
    private TomatoRepository tomatoRepository;

    private TomatoEntity tomatoDto2Entity(TomatoDTO tomatoDTO) {

        TomatoEntity tomatoEntity = new TomatoEntity();
        tomatoEntity.setId(tomatoDTO.getTomatoId());
        BeanUtils.copyProperties(tomatoDTO, tomatoEntity);
        return tomatoEntity;

    }

    private TomatoDTO tomatoEntity2Dto(TomatoEntity tomatoEntity) {
        TomatoDTO tomatoDTO = new TomatoDTO();
        tomatoDTO.setTomatoId(tomatoEntity.getId());
        BeanUtils.copyProperties(tomatoEntity, tomatoDTO);
        return tomatoDTO;
    }

    @Override
    public TomatoDTO create(TomatoDTO tomatoDTO) {
        TomatoEntity tomatoEntity = tomatoDto2Entity(tomatoDTO);
        TomatoEntity savedTomato =  tomatoRepository.save(tomatoEntity);
        return this.tomatoEntity2Dto(savedTomato);
    }

    @Override
    public TomatoDTO retrieve(UUID id) {
        log.info("retrievePotato: {}", id);
        Optional<TomatoEntity> optPotato = tomatoRepository.findById(id);
        if(!optPotato.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Potato Not Found of " + id.toString());
        }
        return optPotato.map(x -> tomatoEntity2Dto(x)).orElse(null);
    }

    @Override
    public TomatoDTO update(TomatoDTO tomatoDto) {
        Preconditions.checkArgument(tomatoDto.getTomatoId() != null);
        Optional<TomatoEntity> optEntity = tomatoRepository.findById(tomatoDto.getTomatoId());
        TomatoEntity newEntity = tomatoDto2Entity(tomatoDto);
        TomatoEntity savedPotato =  tomatoRepository.save(newEntity);

        return this.tomatoEntity2Dto(savedPotato);
    }

    @Override
    public void delete(UUID id) {
        this.tomatoRepository.deleteById(id);
    }

    @Override
    public List<TomatoDTO> list(UUID potatoId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(0, 2);
        Page<TomatoEntity> retPage = null;
        if(potatoId == null) {
            retPage =  this.tomatoRepository.findAll(pageable);
        } else {
            retPage = this.tomatoRepository.findByPotatoId(potatoId, pageable);
        }
        retPage =  this.tomatoRepository.findAll(pageable);
        List<TomatoEntity> retList = retPage.getContent();
        return retList.stream().map(x -> tomatoEntity2Dto(x)).collect(Collectors.toList());
    }
 }
