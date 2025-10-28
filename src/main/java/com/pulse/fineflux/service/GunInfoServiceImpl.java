package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.GunInfo;
import com.pulse.fineflux.repository.GunInfoRepository;
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
                .productName(dto.getProductName())  // Accept productName as part of creation!
                .guns(dto.getGuns())
                .serialNumber(dto.getSerialNumber())
                .currentReading(dto.getCurrentReading())
                .empId(dto.getEmpId())
                .build();

        gunInfoRepository.save(gunInfo);
        log.info("Created GunInfo for organizationId={} productName={}", dto.getOrganizationId(), dto.getProductName());

        return GunInfoResponseDTO.builder()
                .id(gunInfo.getId())
                .organizationId(gunInfo.getOrganizationId())
                .productName(gunInfo.getProductName())
                .guns(gunInfo.getGuns())
                .serialNumber(gunInfo.getSerialNumber())
                .currentReading(gunInfo.getCurrentReading())
                .empId(dto.getEmpId())
                .build();
    }

    @Override
    public GunInfoResponseDTO updateGunInfo(String id, GunInfoUpdateDTO dto) {
        GunInfo gunInfo = gunInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GunInfo not found with id: " + id));

        gunInfo.setOrganizationId(dto.getOrganizationId());
        gunInfo.setProductName(dto.getProductName());
        gunInfo.setGuns(dto.getGuns());
        gunInfo.setSerialNumber(dto.getSerialNumber());
        gunInfo.setCurrentReading(dto.getCurrentReading());
        gunInfo.setEmpId(dto.getEmpId()); // <<< Fix from setId to setEmpId
        gunInfoRepository.save(gunInfo);

        log.info("Updated GunInfo ID={} for organizationId={} productName={}", id, dto.getOrganizationId(), dto.getProductName());

        return GunInfoResponseDTO.builder()
                .id(gunInfo.getId())
                .organizationId(gunInfo.getOrganizationId())
                .productName(gunInfo.getProductName())
                .guns(gunInfo.getGuns())
                .serialNumber(gunInfo.getSerialNumber())
                .currentReading(gunInfo.getCurrentReading())
                .empId(gunInfo.getEmpId())
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
                .productName(gunInfo.getProductName())
                .guns(gunInfo.getGuns())
                .serialNumber(gunInfo.getSerialNumber())
                .currentReading(gunInfo.getCurrentReading())
                .empId(gunInfo.getEmpId())
                .build();
    }

    @Override
    public List<GunInfoResponseDTO> getAllGunInfo(String organizationId) {
        List<GunInfo> gunInfos = gunInfoRepository.findByOrganizationId(organizationId);

        return gunInfos.stream()
                .map(g -> GunInfoResponseDTO.builder()
                        .id(g.getId())
                        .organizationId(g.getOrganizationId())
                        .productName(g.getProductName())
                        .guns(g.getGuns())
                        .serialNumber(g.getSerialNumber())
                        .currentReading(g.getCurrentReading())
                        .empId(g.getEmpId())
                        .build())
                .collect(Collectors.toList());
    }

    // Sync method for use from SalesServiceImpl - keeps gun info up to date with sales
    public void syncGunInfoWithSale(String orgId, String gunName, String productName, double closingStock) {
        gunInfoRepository.findByOrganizationId(orgId).stream()
                .filter(g -> g.getGuns().trim().equalsIgnoreCase(gunName.trim()))
                .forEach(gunInfo -> {
                    gunInfo.setProductName(productName);
                    gunInfo.setCurrentReading(closingStock);
                    gunInfoRepository.save(gunInfo);
                    log.info("GunInfo '{}' for product '{}' currentReading updated to {}", gunInfo.getGuns(), productName, closingStock);
                });
    }
}
