package com.tecsup.petclinic.services;

import com.tecsup.petclinic.dtos.SpecialtyDTO;

import java.util.List;

public interface SpecialtyService {
    List<SpecialtyDTO> findAll();
    List<SpecialtyDTO> findByName(String name);
    List<SpecialtyDTO> findByOffice(String office);
    List<SpecialtyDTO> findByNameAndOffice(String name, String office);
    SpecialtyDTO findById(Integer id);
    SpecialtyDTO create(SpecialtyDTO dto);
    SpecialtyDTO update(Integer id, SpecialtyDTO dto);
    void delete(Integer id);
}
