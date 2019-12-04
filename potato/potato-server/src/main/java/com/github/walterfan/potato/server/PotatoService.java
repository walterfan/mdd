package com.github.walterfan.potato.server;


import com.github.walterfan.potato.common.dto.PotatoDTO;


import java.util.List;
import java.util.UUID;

/**
 * @Author: Walter Fan
 **/

 interface PotatoService {

     PotatoDTO create(PotatoDTO potatoRequest) ;

     PotatoDTO retrieve(UUID id) ;

     PotatoDTO update(PotatoDTO potatoDto);

     void delete(UUID id);

     List<PotatoDTO> search(UUID userId, String keyword, int page, int size) ;

     List<PotatoDTO> list(UUID userId, int page, int size) ;

     void startPotato(UUID potatoId);

     void stopPotato(UUID potatoId);
    
}
