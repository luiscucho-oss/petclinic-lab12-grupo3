package com.tecsup.petclinic.services;

import com.tecsup.petclinic.dtos.SpecialtyDTO;
import com.tecsup.petclinic.entities.Specialty;
import com.tecsup.petclinic.exceptions.InvalidSpecialtyException;
import com.tecsup.petclinic.exceptions.SpecialtyNotFoundException;
import com.tecsup.petclinic.mapper.SpecialtyMapper;
import com.tecsup.petclinic.repositories.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecialtyServiceImpl implements SpecialtyService {
    private final SpecialtyRepository repository;
    private final SpecialtyMapper mapper;

    @Override
    public List<SpecialtyDTO> findAll() {
        return mapper.mapToDtoList(repository.findAll(Sort.by("id")));
    }

    @Override
    public List<SpecialtyDTO> findByName(String name) {
        return mapper.mapToDtoList(repository.findByNameOrderByIdAsc(name));
    }

    @Override
    public List<SpecialtyDTO> findByOffice(String office) {
        return mapper.mapToDtoList(repository.findByOfficeOrderByIdAsc(office));
    }

    @Override
    public List<SpecialtyDTO> findByNameAndOffice(String name, String office) {
        return mapper.mapToDtoList(repository.findByNameAndOfficeOrderByIdAsc(name, office));
    }

    @Override
    public SpecialtyDTO findById(Integer id) {
        return mapper.mapToDto(requireSpecialty(id));
    }

    @Override
    @Transactional
    public SpecialtyDTO create(SpecialtyDTO dto) {
        validate(dto);
        Specialty specialty = mapper.mapToEntity(dto);
        specialty.setId(null);
        return mapper.mapToDto(repository.save(specialty));
    }

    @Override
    @Transactional
    public SpecialtyDTO update(Integer id, SpecialtyDTO dto) {
        Specialty specialty = requireSpecialty(id);
        validate(dto);
        specialty.setName(dto.getName());
        specialty.setOffice(dto.getOffice());
        specialty.setOpenHour(dto.getOpenHour());
        specialty.setCloseHour(dto.getCloseHour());
        return mapper.mapToDto(repository.save(specialty));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.delete(requireSpecialty(id));
    }

    private Specialty requireSpecialty(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new SpecialtyNotFoundException("Specialty not found: " + id));
    }

    private void validate(SpecialtyDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()
                || dto.getOffice() == null || dto.getOffice().isBlank()) {
            throw new InvalidSpecialtyException("Name and office are required");
        }
        if (dto.getName().length() > 80 || dto.getOffice().length() > 40) {
            throw new InvalidSpecialtyException("Specialty field exceeds its maximum length");
        }
        Integer open = dto.getOpenHour();
        Integer close = dto.getCloseHour();
        if (open == null || close == null || open < 0 || open > 23 || close < 0 || close > 23) {
            throw new InvalidSpecialtyException("Hours must be between 0 and 23");
        }
        if (open >= close) {
            throw new InvalidSpecialtyException("Opening hour must be before closing hour");
        }
    }
}
