package com.tecsup.petclinic.repositories;

import com.tecsup.petclinic.entities.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByPet_Id(Integer petId);

    List<Visit> findByVet_Id(Integer vetId);

    List<Visit> findByVisitDateBetween(LocalDate startDate, LocalDate endDate);

    List<Visit> findByVet_IdAndVisitDateBetween(
            Integer vetId, LocalDate startDate, LocalDate endDate);

    long countByPet_Id(Integer petId);

    @Query("SELECT COALESCE(SUM(v.cost), 0) FROM visits v WHERE v.pet.id = :petId")
    BigDecimal sumCostByPetId(@Param("petId") Integer petId);
}
