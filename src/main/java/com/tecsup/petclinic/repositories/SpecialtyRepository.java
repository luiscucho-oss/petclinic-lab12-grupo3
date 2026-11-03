package com.tecsup.petclinic.repositories;

import com.tecsup.petclinic.entities.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface SpecialtyRepository extends JpaRepository<Specialty, Integer> {
    List<Specialty> findByNameOrderByIdAsc(String name);
    List<Specialty> findByOfficeOrderByIdAsc(String office);
    List<Specialty> findByNameAndOfficeOrderByIdAsc(String name, String office);
}
