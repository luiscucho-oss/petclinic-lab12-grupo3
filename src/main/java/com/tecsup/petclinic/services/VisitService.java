package com.tecsup.petclinic.services;

import com.tecsup.petclinic.dtos.VisitDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface VisitService {

    VisitDTO create(VisitDTO visitDTO);

    VisitDTO update(VisitDTO visitDTO);

    void delete(Long id);

    VisitDTO findById(Long id);

    List<VisitDTO> findAll();

    List<VisitDTO> findByPetId(Integer petId);

    List<VisitDTO> findByVetId(Integer vetId);

    List<VisitDTO> findByDateRange(LocalDate startDate, LocalDate endDate);

    List<VisitDTO> findByVetAndDateRange(
            Integer vetId, LocalDate startDate, LocalDate endDate);

    long countByPetId(Integer petId);

    BigDecimal sumCostByPetId(Integer petId);
}
