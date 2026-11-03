package com.tecsup.petclinic.mapper;

import com.tecsup.petclinic.dtos.SpecialtyDTO;
import com.tecsup.petclinic.entities.Specialty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {
    @Mapping(target = "vets", ignore = true)
    Specialty mapToEntity(SpecialtyDTO dto);

    SpecialtyDTO mapToDto(Specialty specialty);

    List<SpecialtyDTO> mapToDtoList(List<Specialty> specialties);
}
