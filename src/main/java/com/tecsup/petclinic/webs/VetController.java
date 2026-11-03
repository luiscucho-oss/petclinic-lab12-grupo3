package com.tecsup.petclinic.webs;

import com.tecsup.petclinic.dtos.VetDTO;
import com.tecsup.petclinic.services.VetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/vets")
@RequiredArgsConstructor
public class VetController {
    private final VetService service;

    @GetMapping
    public List<VetDTO> findAll(@RequestParam(required = false) Boolean active) {
        return active == null ? service.findAll() : service.findByActive(active);
    }

    @GetMapping("/{id}")
    public VetDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<VetDTO> create(@RequestBody VetDTO dto) {
        VetDTO created = service.create(dto);
        return ResponseEntity.created(URI.create("/vets/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public VetDTO update(@PathVariable Integer id, @RequestBody VetDTO dto) {
        return service.update(id, dto);
    }

    @PutMapping("/{id}/deactivate")
    public VetDTO deactivate(@PathVariable Integer id) {
        return service.deactivate(id);
    }

    @PutMapping("/{id}/reactivate")
    public VetDTO reactivate(@PathVariable Integer id) {
        return service.reactivate(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
