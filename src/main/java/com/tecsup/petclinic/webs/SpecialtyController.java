package com.tecsup.petclinic.webs;

import com.tecsup.petclinic.dtos.SpecialtyDTO;
import com.tecsup.petclinic.services.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/specialties")
@RequiredArgsConstructor
public class SpecialtyController {
    private final SpecialtyService service;

    @GetMapping
    public List<SpecialtyDTO> findAll(@RequestParam(required = false) String name,
                                      @RequestParam(required = false) String office) {
        if (name != null && office != null) {
            return service.findByNameAndOffice(name, office);
        }
        if (name != null) {
            return service.findByName(name);
        }
        return office == null ? service.findAll() : service.findByOffice(office);
    }

    @GetMapping("/{id}")
    public SpecialtyDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<SpecialtyDTO> create(@RequestBody SpecialtyDTO dto) {
        SpecialtyDTO created = service.create(dto);
        return ResponseEntity.created(URI.create("/specialties/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public SpecialtyDTO update(@PathVariable Integer id, @RequestBody SpecialtyDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
