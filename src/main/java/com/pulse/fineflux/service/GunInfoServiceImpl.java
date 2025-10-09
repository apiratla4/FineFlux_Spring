package com.pulse.fineflux.service;


import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.GunInfo;
import com.pulse.fineflux.repository.GunInfoRepository;
import com.pulse.fineflux.service.GunInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GunInfoServiceImpl implements GunInfoService {

    private final GunInfoRepository gunInfoRepository;

    @Override
    public GunInfoResponseDTO createGunInfo(GunInfoCreateDTO dto) {
        GunInfo gunInfo = GunInfo.builder()
                .organizationId(dto.getOrganizationId())
                .guns(dto.getGuns())
                .serialNumber(dto.getSerialNumber())
                .currentReading(dto.getCurrentReading())
                .build();

        gunInfoRepository.save(gunInfo);
        log.info("Created GunInfo for organizationId={}", dto.getOrganizationId());

        return GunInfoResponseDTO.builder()
                .id(gunInfo.getId())
                .organizationId(gunInfo.getOrganizationId())
                .guns(gunInfo.getGuns())
                .serialNumber(gunInfo.getSerialNumber())
                .currentReading(gunInfo.getCurrentReading())
                .build();
    }

    @Override
    public GunInfoResponseDTO updateGunInfo(String id, GunInfoUpdateDTO dto) {
        GunInfo gunInfo = gunInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GunInfo not found with id: " + id));

        gunInfo.setOrganizationId(dto.getOrganizationId());
        gunInfo.setGuns(dto.getGuns());
        gunInfo.setSerialNumber(dto.getSerialNumber());
        gunInfo.setCurrentReading(dto.getCurrentReading());

        gunInfoRepository.save(gunInfo);
        log.info("Updated GunInfo ID={} for organizationId={}", id, dto.getOrganizationId());

        return GunInfoResponseDTO.builder()
                .id(gunInfo.getId())
                .organizationId(gunInfo.getOrganizationId())
                .guns(gunInfo.getGuns())
                .serialNumber(gunInfo.getSerialNumber())
                .currentReading(gunInfo.getCurrentReading())

                .build();
    }

    @Override
    public void deleteGunInfo(String id) {
        gunInfoRepository.deleteById(id);
        log.info("Deleted GunInfo ID={}", id);
    }

    @Override
    public GunInfoResponseDTO getGunInfoById(String id) {
        GunInfo gunInfo = gunInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GunInfo not found with id: " + id));

        return GunInfoResponseDTO.builder()
                .id(gunInfo.getId())
                .organizationId(gunInfo.getOrganizationId())
                .guns(gunInfo.getGuns())
                .serialNumber(gunInfo.getSerialNumber())
                .currentReading(gunInfo.getCurrentReading())
                .build();
    }

    @Override
    public List<GunInfoResponseDTO> getAllGunInfo(String organizationId) {
        List<GunInfo> gunInfos = gunInfoRepository.findByOrganizationId(organizationId);

        return gunInfos.stream()
                .map(g -> GunInfoResponseDTO.builder()
                        .id(g.getId())
                        .organizationId(g.getOrganizationId())
                        .guns(g.getGuns())
                        .serialNumber(g.getSerialNumber())
                        .currentReading(g.getCurrentReading())
                        .build())
                .collect(Collectors.toList());
    }
}
