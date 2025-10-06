package com.pulse.fineflux.service;


import com.pulse.fineflux.domain.GunInfoCreateDTO;
import com.pulse.fineflux.domain.GunInfoResponseDTO;
import com.pulse.fineflux.domain.GunInfoUpdateDTO;

import java.util.List;

public interface GunInfoService {
    GunInfoResponseDTO createGunInfo(GunInfoCreateDTO dto);

    GunInfoResponseDTO updateGunInfo(String id, GunInfoUpdateDTO dto);

    void deleteGunInfo(String id);

    GunInfoResponseDTO getGunInfoById(String id);

    List<GunInfoResponseDTO> getAllGunInfo(String organizationId);
}
