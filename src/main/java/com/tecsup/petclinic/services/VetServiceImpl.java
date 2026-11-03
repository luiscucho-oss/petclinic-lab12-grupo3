package com.tecsup.petclinic.services;

import com.tecsup.petclinic.dtos.VetDTO;
import com.tecsup.petclinic.entities.Vet;
import com.tecsup.petclinic.exceptions.InvalidVetException;
import com.tecsup.petclinic.exceptions.VetNotFoundException;
import com.tecsup.petclinic.mapper.VetMapper;
import com.tecsup.petclinic.repositories.VetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VetServiceImpl implements VetService {
    private final VetRepository repository;
    private final VetMapper mapper;

    @Override
    public List<VetDTO> findAll() {
        return mapper.mapToDtoList(repository.findAll(Sort.by("id")));
    }

    @Override
    public List<VetDTO> findByActive(boolean active) {
        return mapper.mapToDtoList(repository.findByActiveOrderByIdAsc(active));
    }

    @Override
    public VetDTO findById(Integer id) {
        return mapper.mapToDto(requireVet(id));
    }

    @Override
    @Transactional
    public VetDTO create(VetDTO dto) {
        validate(dto);
        Vet vet = mapper.mapToEntity(dto);
        vet.setId(null);
        return mapper.mapToDto(repository.save(vet));
    }

    @Override
    @Transactional
    public VetDTO update(Integer id, VetDTO dto) {
        Vet vet = requireVet(id);
        validate(dto);
        vet.setFirstName(dto.getFirstName());
        vet.setLastName(dto.getLastName());
        vet.setEmail(dto.getEmail());
        vet.setPhone(dto.getPhone());
        vet.setActive(dto.getActive());
        return mapper.mapToDto(repository.save(vet));
    }

    @Override
    @Transactional
    public VetDTO deactivate(Integer id) {
        return setActive(id, false);
    }

    @Override
    @Transactional
    public VetDTO reactivate(Integer id) {
        return setActive(id, true);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.delete(requireVet(id));
    }

    private VetDTO setActive(Integer id, boolean active) {
        Vet vet = requireVet(id);
        vet.setActive(active);
        return mapper.mapToDto(repository.save(vet));
    }

    private Vet requireVet(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new VetNotFoundException("Vet not found: " + id));
    }

    private void validate(VetDTO dto) {
        if (dto == null || dto.getFirstName() == null || dto.getFirstName().isBlank()
                || dto.getLastName() == null || dto.getLastName().isBlank()) {
            throw new InvalidVetException("First name and last name are required");
        }
        if (dto.getFirstName().length() > 30 || dto.getLastName().length() > 30
                || (dto.getEmail() != null && dto.getEmail().length() > 100)
                || (dto.getPhone() != null && dto.getPhone().length() > 20)) {
            throw new InvalidVetException("Vet field exceeds its maximum length");
        }
        if (dto.getActive() == null) {
            throw new InvalidVetException("Active must be true or false");
        }
    }
}
