package com.tecsup.petclinic.webs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecsup.petclinic.dtos.VetDTO;
import com.tecsup.petclinic.support.IntegrationTest;
import com.tecsup.petclinic.support.VetTestData;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class VetControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void testFindAllVets() throws Exception {
        mvc.perform(get("/vets"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].lastName", is("Carter")));
    }

    @Test
    void testFindVetOK() throws Exception {
        mvc.perform(get("/vets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("James")))
                .andExpect(jsonPath("$.lastName", is("Carter")))
                .andExpect(jsonPath("$.email", is("james.carter@petclinic.com")))
                .andExpect(jsonPath("$.phone", is("6085551234")))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.specialties").doesNotExist());
    }

    @Test
    void testFindVetKO() throws Exception {
        mvc.perform(get("/vets/666")).andExpect(status().isNotFound());
    }

    @Test
    void testCreateVet() throws Exception {
        VetDTO created = createVet(VetTestData.newVet());
        flushAndClear();
        assertEquals("Torres", jdbc.queryForObject(
                "SELECT last_name FROM vets WHERE id = ?", String.class, created.getId()));
        assertEquals("lucia.torres@petclinic.com", jdbc.queryForObject(
                "SELECT email FROM vets WHERE id = ?", String.class, created.getId()));
        mvc.perform(get("/vets/{id}", created.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.phone", is("999111222")));
    }

    @Test
    void testCreateVetDoesNotOverwriteAnExistingId() throws Exception {
        VetDTO input = VetTestData.newVet();
        input.setId(1);
        VetDTO created = createVet(input);
        assertNotEquals(1, created.getId());
        flushAndClear();
        assertEquals("Carter", jdbc.queryForObject("SELECT last_name FROM vets WHERE id = 1", String.class));
        assertEquals(7, jdbc.queryForObject("SELECT COUNT(*) FROM vets", Integer.class));
    }

    @Test
    void testCreateVetDefaultsToActive() throws Exception {
        mvc.perform(post("/vets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ana\",\"lastName\":\"Perez\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.active", is(true)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void testCreateVetRejectsBlankLastName(String lastName) throws Exception {
        VetDTO input = VetTestData.newVet();
        input.setLastName(lastName);
        expectInvalidCreate(input);
    }

    @Test
    void testCreateVetRejectsMissingFirstName() throws Exception {
        VetDTO input = VetTestData.newVet();
        input.setFirstName(null);
        expectInvalidCreate(input);
    }

    @Test
    void testCreateVetRejectsNullActive() throws Exception {
        VetDTO input = VetTestData.newVet();
        input.setActive(null);
        expectInvalidCreate(input);
    }

    @Test
    void testCreateVetRejectsOversizedPhone() throws Exception {
        VetDTO input = VetTestData.newVet();
        input.setPhone("1".repeat(21));
        expectInvalidCreate(input);
    }

    @Test
    void testUpdateVet() throws Exception {
        VetDTO created = createVet(VetTestData.newVet());
        VetDTO update = VetTestData.newVet();
        update.setId(1);
        update.setLastName("Sanchez");
        update.setEmail("lucia.sanchez@petclinic.com");
        update.setActive(false);
        mvc.perform(put("/vets/{id}", created.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(update)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(created.getId())))
                .andExpect(jsonPath("$.lastName", is("Sanchez")))
                .andExpect(jsonPath("$.active", is(false)));
        flushAndClear();
        mvc.perform(get("/vets/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("lucia.sanchez@petclinic.com")))
                .andExpect(jsonPath("$.lastName", is("Sanchez")));
        assertEquals("Carter", jdbc.queryForObject("SELECT last_name FROM vets WHERE id = 1", String.class));
    }

    @Test
    void testUpdateVetPreservesSpecialtyAssignments() throws Exception {
        mvc.perform(put("/vets/2").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(VetTestData.newVet())))
                .andExpect(status().isOk());
        flushAndClear();
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM vet_specialties WHERE vet_id = 2 AND specialty_id = 1", Integer.class));
    }

    @Test
    void testUpdateVetKO() throws Exception {
        mvc.perform(put("/vets/666").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(VetTestData.newVet())))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateVetRejectsInvalidDataWithoutChangingTheRecord() throws Exception {
        VetDTO update = VetTestData.newVet();
        update.setLastName(null);
        mvc.perform(put("/vets/1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(update)))
                .andExpect(status().isBadRequest());
        assertEquals("Carter", jdbc.queryForObject("SELECT last_name FROM vets WHERE id = 1", String.class));
    }

    @Test
    void testDeactivateVet() throws Exception {
        VetDTO created = createVet(VetTestData.newVet());
        mvc.perform(put("/vets/{id}/deactivate", created.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active", is(false)));
        flushAndClear();
        assertEquals(false, jdbc.queryForObject(
                "SELECT active FROM vets WHERE id = ?", Boolean.class, created.getId()));
    }

    @Test
    void testReactivateVet() throws Exception {
        mvc.perform(put("/vets/6/reactivate"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(6)))
                .andExpect(jsonPath("$.active", is(true)));
        flushAndClear();
        assertEquals(true, jdbc.queryForObject("SELECT active FROM vets WHERE id = 6", Boolean.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"deactivate", "reactivate"})
    void testChangeActiveVetKO(String action) throws Exception {
        mvc.perform(put("/vets/666/{action}", action)).andExpect(status().isNotFound());
    }

    @Test
    void testFindActiveVets() throws Exception {
        mvc.perform(get("/vets").param("active", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].active", everyItem(is(true))));
    }

    @Test
    void testFindInactiveVets() throws Exception {
        mvc.perform(get("/vets").param("active", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(6))).andExpect(jsonPath("$[0].active", is(false)));
    }

    @Test
    void testDeleteVet() throws Exception {
        VetDTO created = createVet(VetTestData.newVet());
        mvc.perform(delete("/vets/{id}", created.getId())).andExpect(status().isOk());
        flushAndClear();
        mvc.perform(get("/vets/{id}", created.getId())).andExpect(status().isNotFound());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vets WHERE id = ?", Integer.class, created.getId()));
    }

    @Test
    void testDeleteVetPreservesVisitsAndRemovesAssignments() throws Exception {
        mvc.perform(delete("/vets/2")).andExpect(status().isOk());
        flushAndClear();
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vet_specialties WHERE vet_id = 2", Integer.class));
        assertEquals(6, jdbc.queryForObject("SELECT COUNT(*) FROM visits", Integer.class));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM visits WHERE id IN (1, 2) AND vet_id IS NULL", Integer.class));
    }

    @Test
    void testDeleteVetKO() throws Exception {
        mvc.perform(delete("/vets/1000")).andExpect(status().isNotFound());
    }

    @Test
    void testRejectsMalformedRequests() throws Exception {
        mvc.perform(get("/vets/not-a-number")).andExpect(status().isBadRequest());
        mvc.perform(get("/vets").param("active", "invalid")).andExpect(status().isBadRequest());
        mvc.perform(post("/vets").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    private VetDTO createVet(VetDTO input) throws Exception {
        String response = mvc.perform(post("/vets").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(input)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", greaterThan(6)))
                .andExpect(jsonPath("$.firstName", is(input.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(input.getLastName())))
                .andExpect(jsonPath("$.email", is(input.getEmail())))
                .andExpect(jsonPath("$.phone", is(input.getPhone())))
                .andExpect(jsonPath("$.active", is(input.getActive())))
                .andReturn().getResponse().getContentAsString();
        return json.readValue(response, VetDTO.class);
    }

    private void expectInvalidCreate(VetDTO input) throws Exception {
        mvc.perform(post("/vets").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
        assertEquals(6, jdbc.queryForObject("SELECT COUNT(*) FROM vets", Integer.class));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
