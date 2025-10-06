package com.pulse.fineflux.service;


import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.*;
import com.pulse.fineflux.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements SalesService {

    private final SalesRepository salesRepository;
    private final GunInfoRepository gunInfoRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public SalesResponseDTO createSale(SalesCreateDTO dto) {
        try {
            log.info("Creating new Sale for orgId={}", dto.getOrganizationId());

            // Validate employee exists
            if (dto.getEmployeeId() == null || dto.getEmployeeId().isBlank()) {
                throw new RuntimeException("Employee ID cannot be null or empty");
            }

            employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + dto.getEmployeeId()));

            LocalDateTime entryDateTime = dto.getDateTime() != null
                    ? dto.getDateTime()
                    : LocalDateTime.now();

            // Fetch latest gun and product info
            String gunName = gunInfoRepository.findByOrganizationId(dto.getOrganizationId())
                    .stream().findFirst().map(GunInfo::getGuns).orElse("N/A");

            String productName = productRepository.findByOrganizationId(dto.getOrganizationId())
                    .stream().findFirst().map(Product::getProductName).orElse("N/A");

            // Calculate opening stock
            double opening = dto.getOpeningStock() == 0f
                    ? getLastClosing(productName, gunName)
                    : dto.getOpeningStock();

            double closing = dto.getClosingStock();
            double testing = dto.getTestingTotal();

            BigDecimal saleVolume = BigDecimal.valueOf(closing - opening - testing);
            float amount = saleVolume.multiply(BigDecimal.valueOf(dto.getPrice())).floatValue();

            // Save sale
            Sales sale = Sales.builder()
                    .organizationId(dto.getOrganizationId())
                    .dateTime(entryDateTime)
                    .productName(productName)
                    .guns(gunName)
                    .employeeId(dto.getEmployeeId())
                    .openingStock(opening)
                    .closingStock(closing)
                    .testingTotal(testing)
                    .salesInLiters(saleVolume.floatValue())
                    .price(dto.getPrice())
                    .salesInRupees(amount)
                    .build();

            Sales saved = salesRepository.save(sale);
            log.info("Saved sale: {}", saved);


            return toResponse(saved);

        } catch (Exception e) {
            log.error("Failed to create sale for orgId={}", dto.getOrganizationId(), e);
            throw new RuntimeException("Error creating sale: " + e.getMessage());
        }
    }

    /**
     * Fetch last closing stock for a given product and gun
     */
    public double getLastClosing(String productName, String gun) {
        try {
            log.info("Fetching last closing for product {} and gun {}", productName, gun);
            Sales last = salesRepository.findTopByProductNameAndGunsOrderByDateTimeDesc(productName, gun);
            double closing = (last != null) ? last.getClosingStock() : 0f;
            log.info("Last closing found: {}", closing);
            return closing;
        } catch (Exception e) {
            log.error("Error fetching last closing: {}", e.getMessage(), e);
            return 0f;
        }
    }

    @Override
    public SalesResponseDTO updateSale(String id, SalesUpdateDTO dto) {
        try {
            Sales sale = salesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));

            sale.setOpeningStock(dto.getOpeningStock());
            sale.setClosingStock(dto.getClosingStock());
            sale.setTestingTotal(dto.getTestingTotal());
            sale.setSalesInLiters(dto.getSalesInLiters());
            sale.setPrice(dto.getPrice());
            sale.setSalesInRupees(dto.getSalesInRupees());

            Sales updated = salesRepository.save(sale);
            return toResponse(updated);

        } catch (Exception e) {
            log.error("Error updating sale id={}", id, e);
            throw new RuntimeException("Error updating sale: " + e.getMessage());
        }
    }

    @Override
    public void deleteSale(String id) {
        try {
            log.info("Deleting sale id={}", id);
            salesRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting sale id={}", id, e);
            throw new RuntimeException("Error deleting sale: " + e.getMessage());
        }
    }

    @Override
    public SalesResponseDTO getSaleById(String id) {
        return salesRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    @Override
    public List<SalesResponseDTO> getAllSales(String organizationId) {
        return salesRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private SalesResponseDTO toResponse(Sales sale) {
        return SalesResponseDTO.builder()
                .id(sale.getId())
                .organizationId(sale.getOrganizationId())
                .dateTime(sale.getDateTime())
                .productName(sale.getProductName())
                .guns(sale.getGuns())
                .employeeId(sale.getEmployeeId())  // validated/fetched from Employee table
                .openingStock(sale.getOpeningStock())
                .closingStock(sale.getClosingStock())
                .testingTotal(sale.getTestingTotal())
                .salesInLiters(sale.getSalesInLiters())
                .price(sale.getPrice())
                .salesInRupees(sale.getSalesInRupees())
                .build();
    }
}
