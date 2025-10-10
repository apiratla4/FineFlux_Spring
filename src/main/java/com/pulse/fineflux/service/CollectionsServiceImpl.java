package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.Collections;
import com.pulse.fineflux.repository.CollectionsRepository;
import com.pulse.fineflux.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionsServiceImpl implements CollectionsService {

    private final CollectionsRepository collectionsRepository;
    private final SalesRepository salesRepository; // used to calculate expected total

    @Override
    public CollectionsResponseDTO create(CollectionsCreateDTO dto) {
        log.info("Creating collection for org: {}", dto.getOrganizationId());

        Collections entity = new Collections();
        BeanUtils.copyProperties(dto, entity);

        // Calculate totals from Sales
        double expected = salesRepository
                .findByOrganizationIdAndDateTime(dto.getOrganizationId(), dto.getDateTime())
                .stream()
                .mapToDouble(s -> s.getSalesInRupees())
                .sum();

        double received = dto.getCashReceived() + dto.getPhonePay() + dto.getCreditCard();
        entity.setExpectedTotal(expected);
        entity.setReceivedTotal(received);


        Collections saved = collectionsRepository.save(entity);
        return convertToResponse(saved);
    }

    @Override
    public CollectionsResponseDTO update(String id, CollectionsUpdateDTO dto) {
        Collections existing = collectionsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        BeanUtils.copyProperties(dto, existing, "id", "organizationId");

        double expected = existing.getExpectedTotal(); // Keep existing expected or recompute
        double received = dto.getCashReceived() + dto.getPhonePay() + dto.getCreditCard();
        existing.setReceivedTotal(received);

        Collections saved = collectionsRepository.save(existing);
        return convertToResponse(saved);
    }

    @Override
    public CollectionsResponseDTO getById(String id) {
        Collections entity = collectionsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));
        return convertToResponse(entity);
    }

    @Override
    public List<CollectionsResponseDTO> getAll(String organizationId) {
        log.info("Fetching all collections for organizationId: {}", organizationId);
        return collectionsRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public void delete(String id) {
        collectionsRepository.deleteById(id);
    }

    private CollectionsResponseDTO convertToResponse(Collections entity) {
        CollectionsResponseDTO dto = new CollectionsResponseDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
