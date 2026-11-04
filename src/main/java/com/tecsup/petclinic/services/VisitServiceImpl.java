package com.tecsup.petclinic.services;

import com.tecsup.petclinic.dtos.VisitDTO;
import com.tecsup.petclinic.entities.Pet;
import com.tecsup.petclinic.entities.Vet;
import com.tecsup.petclinic.entities.Visit;
import com.tecsup.petclinic.exceptions.VisitNotFoundException;
import com.tecsup.petclinic.mapper.VisitMapper;
import com.tecsup.petclinic.repositories.PetRepository;
import com.tecsup.petclinic.repositories.VetRepository;
import com.tecsup.petclinic.repositories.VisitRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    private final PetRepository petRepository;
    private final VetRepository vetRepository;
    private final VisitMapper visitMapper;

    public VisitServiceImpl(
            VisitRepository visitRepository,
            PetRepository petRepository,
            VetRepository vetRepository,
            VisitMapper visitMapper) {
        this.visitRepository = visitRepository;
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.visitMapper = visitMapper;
    }

    @Override
    public VisitDTO create(VisitDTO dto) {
        validateVisit(dto);

        Visit visit = new Visit();
        copyData(dto, visit);

        return visitMapper.mapToDto(visitRepository.save(visit));
    }

    @Override
    public VisitDTO update(VisitDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("Visit ID is required");
        }

        validateVisit(dto);

        Visit visit = visitRepository.findById(dto.getId())
                .orElseThrow(() -> new VisitNotFoundException("Visit not found"));

        copyData(dto, visit);

        return visitMapper.mapToDto(visitRepository.save(visit));
    }

    @Override
    public void delete(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new VisitNotFoundException("Visit not found"));

        visitRepository.delete(visit);
    }

    @Override
    public VisitDTO findById(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new VisitNotFoundException("Visit not found"));

        return visitMapper.mapToDto(visit);
    }

    @Override
    public List<VisitDTO> findAll() {
        return visitRepository.findAll()
                .stream()
                .map(visitMapper::mapToDto)
                .toList();
    }

    @Override
    public List<VisitDTO> findByPetId(Integer petId) {
        return visitRepository.findByPet_Id(petId)
                .stream()
                .map(visitMapper::mapToDto)
                .toList();
    }

    @Override
    public List<VisitDTO> findByVetId(Integer vetId) {
        return visitRepository.findByVet_Id(vetId)
                .stream()
                .map(visitMapper::mapToDto)
                .toList();
    }

    @Override
    public List<VisitDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);

        return visitRepository.findByVisitDateBetween(startDate, endDate)
                .stream()
                .map(visitMapper::mapToDto)
                .toList();
    }

    @Override
    public List<VisitDTO> findByVetAndDateRange(
            Integer vetId, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);

        return visitRepository.findByVet_IdAndVisitDateBetween(
                        vetId, startDate, endDate)
                .stream()
                .map(visitMapper::mapToDto)
                .toList();
    }

    @Override
    public long countByPetId(Integer petId) {
        return visitRepository.countByPet_Id(petId);
    }

    @Override
    public BigDecimal sumCostByPetId(Integer petId) {
        BigDecimal total = visitRepository.sumCostByPetId(petId);
        return total != null ? total : BigDecimal.ZERO;
    }

    private void validateVisit(VisitDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Visit data is required");
        }
        if (dto.getVisitDate() == null) {
            throw new IllegalArgumentException("Visit date is required");
        }
        if (dto.getPetId() == null) {
            throw new IllegalArgumentException("Pet ID is required");
        }
        if (dto.getCost() != null && dto.getCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost cannot be negative");
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }
    }

    private void copyData(VisitDTO dto, Visit visit) {
        Pet pet = petRepository.findById(dto.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("Pet not found"));

        Vet vet = null;
        if (dto.getVetId() != null) {
            vet = vetRepository.findById(dto.getVetId())
                    .orElseThrow(() -> new IllegalArgumentException("Vet not found"));
        }

        visit.setVisitDate(dto.getVisitDate());
        visit.setDescription(dto.getDescription());
        visit.setCost(dto.getCost());
        visit.setPet(pet);
        visit.setVet(vet);
    }
}
