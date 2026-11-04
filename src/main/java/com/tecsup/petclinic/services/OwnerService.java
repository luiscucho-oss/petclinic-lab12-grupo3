package com.tecsup.petclinic.services;

import com.tecsup.petclinic.dtos.OwnerDTO;
import java.util.List;

public interface OwnerService {

    OwnerDTO create(OwnerDTO ownerDTO);

    OwnerDTO update(OwnerDTO ownerDTO);

    void delete(Long id);

    OwnerDTO findById(Long id);

    List<OwnerDTO> findByLastName(String lastName);

    List<OwnerDTO> findAll();
}