package com.tecsup.petclinic.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@IntegrationTest
class DatabaseSeedTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void loadsTheSharedSeedData() {
        assertAll(
                () -> assertEquals(6, count("vets")),
                () -> assertEquals(3, count("specialties")),
                () -> assertEquals(10, count("owners")),
                () -> assertEquals(13, count("pets")),
                () -> assertEquals(6, count("visits")),
                () -> assertEquals(8, count("types")),
                () -> assertEquals(5, count("vet_specialties"))
        );
    }

    @Test
    void preservesTheReferenceRecordsAndRelationships() {
        assertAll(
                () -> assertEquals("Carter", jdbc.queryForObject(
                        "SELECT last_name FROM vets WHERE id = 1", String.class)),
                () -> assertEquals("Franklin", jdbc.queryForObject(
                        "SELECT last_name FROM owners WHERE id = 1", String.class)),
                () -> assertEquals("radiology", jdbc.queryForObject(
                        "SELECT name FROM specialties WHERE id = 1", String.class)),
                () -> assertEquals(2, jdbc.queryForObject(
                        "SELECT COUNT(*) FROM visits WHERE pet_id = 7", Integer.class)),
                () -> assertEquals(new BigDecimal("320.00"), jdbc.queryForObject(
                        "SELECT SUM(cost) FROM visits WHERE pet_id = 7", BigDecimal.class))
        );
    }

    private int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }
}
