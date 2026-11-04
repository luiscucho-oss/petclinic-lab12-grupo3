package com.tecsup.petclinic.webs;

import com.tecsup.petclinic.dtos.VisitDTO;
import com.tecsup.petclinic.exceptions.VisitNotFoundException;
import com.tecsup.petclinic.services.VisitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping("/visits")
    public ResponseEntity<List<VisitDTO>> findAll() {
        return ResponseEntity.ok(visitService.findAll());
    }

    @GetMapping("/visits/{id}")
    public ResponseEntity<VisitDTO> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(visitService.findById(id));
        } catch (VisitNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/visits")
    public ResponseEntity<VisitDTO> create(@RequestBody VisitDTO dto) {
        try {
            VisitDTO created = visitService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/visits/{id}")
    public ResponseEntity<VisitDTO> update(
            @PathVariable Long id,
            @RequestBody VisitDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(visitService.update(dto));
        } catch (VisitNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/visits/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            visitService.delete(id);
            return ResponseEntity.ok("Deleted Visit ID: " + id);
        } catch (VisitNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/visits/pet/{petId}")
    public ResponseEntity<List<VisitDTO>> findByPetId(
            @PathVariable Integer petId) {
        return ResponseEntity.ok(visitService.findByPetId(petId));
    }

    @GetMapping("/visits/vet/{vetId}")
    public ResponseEntity<List<VisitDTO>> findByVetId(
            @PathVariable Integer vetId) {
        return ResponseEntity.ok(visitService.findByVetId(vetId));
    }

    @GetMapping("/visits/date-range")
    public ResponseEntity<List<VisitDTO>> findByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            return ResponseEntity.ok(
                    visitService.findByDateRange(startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/visits/vet/{vetId}/date-range")
    public ResponseEntity<List<VisitDTO>> findByVetAndDateRange(
            @PathVariable Integer vetId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            return ResponseEntity.ok(
                    visitService.findByVetAndDateRange(
                            vetId, startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/visits/pet/{petId}/count")
    public ResponseEntity<Long> countByPetId(
            @PathVariable Integer petId) {
        return ResponseEntity.ok(visitService.countByPetId(petId));
    }

    @GetMapping("/visits/pet/{petId}/total-cost")
    public ResponseEntity<BigDecimal> sumCostByPetId(
            @PathVariable Integer petId) {
        return ResponseEntity.ok(visitService.sumCostByPetId(petId));
    }
}