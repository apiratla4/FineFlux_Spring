
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.Organization;
import com.pulse.fineflux.repository.OrganizationRepository;
import com.pulse.fineflux.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository repo;

    public OrganizationServiceImpl(OrganizationRepository repo) {
        this.repo = repo;
    }

    @Override
    public OrganizationResponse create(OrganizationCreateRequest req) {
        log.info("Creating organization with organizationId={}", req.organizationId);
        if (repo.existsByOrganizationId(req.organizationId)) {
            log.warn("organizationId={} already exists", req.organizationId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organizationId already exists");
        }
        Organization o = Organization.builder()
                .organizationId(req.organizationId)
                .organizationName(req.organizationName)
                .gstNumber(req.gstNumber != null ? req.gstNumber.toUpperCase() : null)
                .address1(req.address1)
                .address2(req.address2)
                .city(req.city)
                .state(req.state)
                .country(req.country)
                .postalCode(req.postalCode)
                .phoneNumber(req.phoneNumber)
                .email(req.email)
                .licenseNumber(req.licenseNumber)
                .ownerFirstName(req.ownerFirstName)
                .ownerLastName(req.ownerLastName)
                .build();
        o = repo.save(o);
        log.info("Created organization id={} organizationId={}", o.getId(), o.getOrganizationId());
        return toResponse(o);
    }

    @Override
    public OrganizationResponse getById(String id) {
        log.debug("Fetching organization by mongoId={}", id);
        Organization o = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Organization not found mongoId={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
                });
        return toResponse(o);
    }

    @Override
    public OrganizationResponse getByOrgId(String organizationId) {
        log.debug("Fetching organization by organizationId={}", organizationId);
        Organization o = repo.findByOrganizationId(organizationId)
                .orElseThrow(() -> {
                    log.warn("Organization not found organizationId={}", organizationId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
                });
        return toResponse(o);
    }

    @Override
    public Page<OrganizationResponse> list(Pageable pageable) {
        log.debug("Listing organizations page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repo.findAll(pageable).map(this::toResponse);
    }

    @Override
    public OrganizationResponse update(String id, OrganizationUpdateRequest req) {
        log.info("Updating organization mongoId={}", id);
        Organization o = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Organization not found mongoId={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
                });

        if (req.organizationName != null) o.setOrganizationName(req.organizationName);
        if (req.gstNumber != null) o.setGstNumber(req.gstNumber.toUpperCase());
        if (req.address1 != null) o.setAddress1(req.address1);
        if (req.address2 != null) o.setAddress2(req.address2);
        if (req.city != null) o.setCity(req.city);
        if (req.state != null) o.setState(req.state);
        if (req.country != null) o.setCountry(req.country);
        if (req.postalCode != null) o.setPostalCode(req.postalCode);
        if (req.phoneNumber != null) o.setPhoneNumber(req.phoneNumber);
        if (req.email != null) o.setEmail(req.email);
        if (req.licenseNumber != null) o.setLicenseNumber(req.licenseNumber);
        if (req.ownerFirstName != null) o.setOwnerFirstName(req.ownerFirstName);
        if (req.ownerLastName != null) o.setOwnerLastName(req.ownerLastName);

        o = repo.save(o);
        log.info("Updated organization mongoId={} organizationId={}", o.getId(), o.getOrganizationId());
        return toResponse(o);
    }

    @Override
    public void delete(String id) {
        log.info("Deleting organization mongoId={}", id);
        if (!repo.existsById(id)) {
            log.warn("Delete failed, organization not found mongoId={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
        }
        repo.deleteById(id);
        log.info("Deleted organization mongoId={}", id);
    }

    private OrganizationResponse toResponse(Organization o) {
        OrganizationResponse r = new OrganizationResponse();
        r.id = o.getId();
        r.organizationId = o.getOrganizationId();
        r.organizationName = o.getOrganizationName();
        r.gstNumber = o.getGstNumber();
        r.address1 = o.getAddress1();
        r.address2 = o.getAddress2();
        r.city = o.getCity();
        r.state = o.getState();
        r.country = o.getCountry();
        r.postalCode = o.getPostalCode();
        r.phoneNumber = o.getPhoneNumber();
        r.email = o.getEmail();
        r.licenseNumber = o.getLicenseNumber();
        r.ownerFirstName = o.getOwnerFirstName();
        r.ownerLastName = o.getOwnerLastName();
        return r;
    }
}
