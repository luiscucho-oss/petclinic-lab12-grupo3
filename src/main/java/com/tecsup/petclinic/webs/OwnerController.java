package com.tecsup.petclinic.webs;

import com.tecsup.petclinic.dtos.OwnerDTO;
import com.tecsup.petclinic.exceptions.OwnerNotFoundException;
import com.tecsup.petclinic.services.OwnerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping("/owners")
    public ResponseEntity<List<OwnerDTO>> findAll() {
        return ResponseEntity.ok(ownerService.findAll());
    }

    @GetMapping("/owners/search")
    public ResponseEntity<List<OwnerDTO>> findByLastName(
            @RequestParam String lastName) {
        try {
            return ResponseEntity.ok(ownerService.findByLastName(lastName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/owners/{id}")
    public ResponseEntity<OwnerDTO> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ownerService.findById(id));
        } catch (OwnerNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/owners")
    public ResponseEntity<OwnerDTO> create(@RequestBody OwnerDTO ownerDTO) {
        try {
            OwnerDTO created = ownerService.create(ownerDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/owners/{id}")
    public ResponseEntity<OwnerDTO> update(
            @PathVariable Long id,
            @RequestBody OwnerDTO ownerDTO) {
        try {
            ownerDTO.setId(id);
            return ResponseEntity.ok(ownerService.update(ownerDTO));
        } catch (OwnerNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/owners/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            ownerService.delete(id);
            return ResponseEntity.ok("Deleted Owner ID: " + id);
        } catch (OwnerNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}