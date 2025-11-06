package com.pulse.fineflux.service;


import com.pulse.fineflux.domain.*;
import java.util.List;

public interface CollectionsService {
    CollectionsResponseDTO create(CollectionsCreateDTO dto);
    CollectionsResponseDTO update(String id, CollectionsUpdateDTO dto);
    List<CollectionsResponseDTO> getAll(String organizationId);
    void delete(String id);
    CollectionsResponseDTO getBySaleId(String saleId);

}
