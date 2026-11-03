package com.tecsup.petclinic.services;

import com.tecsup.petclinic.dtos.VetDTO;

import java.util.List;

public interface VetService {
    List<VetDTO> findAll();
    List<VetDTO> findByActive(boolean active);
    VetDTO findById(Integer id);
    VetDTO create(VetDTO dto);
    VetDTO update(Integer id, VetDTO dto);
    VetDTO deactivate(Integer id);
    VetDTO reactivate(Integer id);
    void delete(Integer id);
}
