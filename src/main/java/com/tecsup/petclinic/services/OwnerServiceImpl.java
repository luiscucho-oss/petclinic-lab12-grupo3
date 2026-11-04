package com.tecsup.petclinic.services;

import com.tecsup.petclinic.dtos.OwnerDTO;
import com.tecsup.petclinic.entities.Owner;
import com.tecsup.petclinic.exceptions.OwnerNotFoundException;
import com.tecsup.petclinic.mapper.OwnerMapper;
import com.tecsup.petclinic.repositories.OwnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerServiceImpl implements OwnerService {

    private final OwnerRepository ownerRepository;
    private final OwnerMapper ownerMapper;

    public OwnerServiceImpl(OwnerRepository ownerRepository, OwnerMapper ownerMapper) {
        this.ownerRepository = ownerRepository;
        this.ownerMapper = ownerMapper;
    }

    @Override
    public OwnerDTO create(OwnerDTO ownerDTO) {
        validateOwner(ownerDTO);

        Owner owner = ownerMapper.mapToEntity(ownerDTO);
        owner.setId(null);

        return ownerMapper.mapToDto(ownerRepository.save(owner));
    }

    @Override
    public OwnerDTO update(OwnerDTO ownerDTO) {
        if (ownerDTO == null || ownerDTO.getId() == null) {
            throw new IllegalArgumentException("Owner ID is required");
        }

        validateOwner(ownerDTO);

        Owner owner = ownerRepository.findById(ownerDTO.getId())
                .orElseThrow(() -> new OwnerNotFoundException(
                        "Owner not found: " + ownerDTO.getId()));

        owner.setFirstName(ownerDTO.getFirstName());
        owner.setLastName(ownerDTO.getLastName());
        owner.setAddress(ownerDTO.getAddress());
        owner.setCity(ownerDTO.getCity());
        owner.setTelephone(ownerDTO.getTelephone());

        return ownerMapper.mapToDto(ownerRepository.save(owner));
    }

    @Override
    public void delete(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new OwnerNotFoundException("Owner not found: " + id));

        ownerRepository.delete(owner);
    }

    @Override
    public OwnerDTO findById(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new OwnerNotFoundException("Owner not found: " + id));

        return ownerMapper.mapToDto(owner);
    }

    @Override
    public List<OwnerDTO> findByLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        return ownerRepository.findByLastNameContainingIgnoreCase(lastName)
                .stream()
                .map(ownerMapper::mapToDto)
                .toList();
    }

    @Override
    public List<OwnerDTO> findAll() {
        return ownerRepository.findAll()
                .stream()
                .map(ownerMapper::mapToDto)
                .toList();
    }

    private void validateOwner(OwnerDTO ownerDTO) {
        if (ownerDTO == null) {
            throw new IllegalArgumentException("Owner data is required");
        }

        if (ownerDTO.getLastName() == null || ownerDTO.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
    }
}