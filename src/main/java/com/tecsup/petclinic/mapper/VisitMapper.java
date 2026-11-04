package com.tecsup.petclinic.mapper;

import com.tecsup.petclinic.dtos.VisitDTO;
import com.tecsup.petclinic.entities.Visit;
import org.springframework.stereotype.Component;

@Component
public class VisitMapper {

    public VisitDTO mapToDto(Visit visit) {
        if (visit == null) return null;

        return VisitDTO.builder()
                .id(visit.getId())
                .visitDate(visit.getVisitDate())
                .description(visit.getDescription())
                .cost(visit.getCost())
                .petId(visit.getPet() != null ? visit.getPet().getId() : null)
                .vetId(visit.getVet() != null ? visit.getVet().getId() : null)
                .build();
    }
}
