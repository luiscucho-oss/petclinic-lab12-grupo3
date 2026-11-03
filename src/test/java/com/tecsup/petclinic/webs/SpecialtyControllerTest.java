package com.tecsup.petclinic.webs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tecsup.petclinic.dtos.SpecialtyDTO;
import com.tecsup.petclinic.support.IntegrationTest;
import com.tecsup.petclinic.support.SpecialtyTestData;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
class SpecialtyControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void testFindAllSpecialties() throws Exception {
        mvc.perform(get("/specialties"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3))).andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void testFindSpecialtyOK() throws Exception {
        mvc.perform(get("/specialties/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("radiology")))
                .andExpect(jsonPath("$.office", is("Farewell")))
                .andExpect(jsonPath("$.openHour", is(8)))
                .andExpect(jsonPath("$.closeHour", is(18)))
                .andExpect(jsonPath("$.vets").doesNotExist());
    }

    @Test
    void testFindSpecialtyKO() throws Exception {
        mvc.perform(get("/specialties/666")).andExpect(status().isNotFound());
    }

    @Test
    void testCreateSpecialty() throws Exception {
        SpecialtyDTO created = createSpecialty(SpecialtyTestData.newSpecialty());
        flushAndClear();
        assertEquals("Centro", jdbc.queryForObject(
                "SELECT office FROM specialties WHERE id = ?", String.class, created.getId()));
        assertEquals(8, jdbc.queryForObject(
                "SELECT h_open FROM specialties WHERE id = ?", Integer.class, created.getId()));
        mvc.perform(get("/specialties/{id}", created.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name", is("cardiology")));
    }

    @Test
    void testCreateSpecialtyDoesNotOverwriteAnExistingId() throws Exception {
        SpecialtyDTO input = SpecialtyTestData.newSpecialty();
        input.setId(1);
        SpecialtyDTO created = createSpecialty(input);
        assertNotEquals(1, created.getId());
        flushAndClear();
        assertEquals("radiology", jdbc.queryForObject("SELECT name FROM specialties WHERE id = 1", String.class));
        assertEquals(4, jdbc.queryForObject("SELECT COUNT(*) FROM specialties", Integer.class));
    }

    @Test
    void testFindByName() throws Exception {
        mvc.perform(get("/specialties").param("name", "surgery"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2))).andExpect(jsonPath("$[0].name", is("surgery")));
    }

    @Test
    void testFindByOffice() throws Exception {
        mvc.perform(get("/specialties").param("office", "Farewell"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("radiology")));
    }

    @Test
    void testFindByNameAndOffice() throws Exception {
        mvc.perform(get("/specialties").param("name", "radiology").param("office", "Farewell"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
        mvc.perform(get("/specialties").param("name", "radiology").param("office", "Maryland"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testFindByNameWithoutMatches() throws Exception {
        mvc.perform(get("/specialties").param("name", "unknown"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @CsvSource({"8,8", "18,8"})
    void testCreateSpecialty_InvalidSchedule(int open, int close) throws Exception {
        expectInvalidSchedule(open, close);
    }

    @ParameterizedTest
    @CsvSource({"-1,18", "8,24", "24,25", "0,-1"})
    void testCreateSpecialty_InvalidHourRange(int open, int close) throws Exception {
        expectInvalidSchedule(open, close);
    }

    @Test
    void testCreateSpecialtyAcceptsHourBoundaries() throws Exception {
        SpecialtyDTO input = SpecialtyTestData.newSpecialty();
        input.setOpenHour(0);
        input.setCloseHour(23);
        createSpecialty(input);
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "office", "openHour", "closeHour"})
    void testCreateSpecialtyRejectsMissingFields(String field) throws Exception {
        ObjectNode input = json.valueToTree(SpecialtyTestData.newSpecialty());
        input.remove(field);
        mvc.perform(post("/specialties").contentType(MediaType.APPLICATION_JSON).content(input.toString()))
                .andExpect(status().isBadRequest());
        assertEquals(3, jdbc.queryForObject("SELECT COUNT(*) FROM specialties", Integer.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "office"})
    void testCreateSpecialtyRejectsBlankFields(String field) throws Exception {
        ObjectNode input = json.valueToTree(SpecialtyTestData.newSpecialty());
        input.put(field, "   ");
        mvc.perform(post("/specialties").contentType(MediaType.APPLICATION_JSON).content(input.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateSpecialtyRejectsOversizedOffice() throws Exception {
        SpecialtyDTO input = SpecialtyTestData.newSpecialty();
        input.setOffice("x".repeat(41));
        mvc.perform(post("/specialties").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateSpecialty() throws Exception {
        SpecialtyDTO created = createSpecialty(SpecialtyTestData.newSpecialty());
        SpecialtyDTO update = SpecialtyTestData.newSpecialty();
        update.setId(1);
        update.setName("neurology");
        update.setOffice("Norte");
        update.setOpenHour(9);
        update.setCloseHour(19);
        mvc.perform(put("/specialties/{id}", created.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(update)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(created.getId())))
                .andExpect(jsonPath("$.name", is("neurology")))
                .andExpect(jsonPath("$.office", is("Norte")));
        flushAndClear();
        mvc.perform(get("/specialties/{id}", created.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.openHour", is(9)))
                .andExpect(jsonPath("$.closeHour", is(19))).andExpect(jsonPath("$.name", is("neurology")));
        assertEquals("radiology", jdbc.queryForObject("SELECT name FROM specialties WHERE id = 1", String.class));
    }

    @Test
    void testUpdateSpecialtyPreservesVetAssignments() throws Exception {
        mvc.perform(put("/specialties/1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(SpecialtyTestData.newSpecialty())))
                .andExpect(status().isOk());
        flushAndClear();
        assertEquals(2, jdbc.queryForObject(
                "SELECT COUNT(*) FROM vet_specialties WHERE specialty_id = 1", Integer.class));
    }

    @Test
    void testUpdateSpecialtyKO() throws Exception {
        mvc.perform(put("/specialties/666").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(SpecialtyTestData.newSpecialty())))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateSpecialtyRejectsInvalidScheduleWithoutChangingTheRecord() throws Exception {
        SpecialtyDTO update = SpecialtyTestData.newSpecialty();
        update.setOpenHour(20);
        mvc.perform(put("/specialties/1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(update)))
                .andExpect(status().isBadRequest());
        assertEquals("radiology", jdbc.queryForObject("SELECT name FROM specialties WHERE id = 1", String.class));
        assertEquals(8, jdbc.queryForObject("SELECT h_open FROM specialties WHERE id = 1", Integer.class));
    }

    @Test
    void testDeleteSpecialty() throws Exception {
        SpecialtyDTO created = createSpecialty(SpecialtyTestData.newSpecialty());
        mvc.perform(delete("/specialties/{id}", created.getId())).andExpect(status().isOk());
        flushAndClear();
        mvc.perform(get("/specialties/{id}", created.getId())).andExpect(status().isNotFound());
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM specialties WHERE id = ?", Integer.class, created.getId()));
    }

    @Test
    void testDeleteSpecialtyRemovesAssignmentsAndPreservesVets() throws Exception {
        mvc.perform(delete("/specialties/1")).andExpect(status().isOk());
        flushAndClear();
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM vet_specialties WHERE specialty_id = 1", Integer.class));
        assertEquals(6, jdbc.queryForObject("SELECT COUNT(*) FROM vets", Integer.class));
    }

    @Test
    void testDeleteSpecialtyKO() throws Exception {
        mvc.perform(delete("/specialties/1000")).andExpect(status().isNotFound());
    }

    @Test
    void testRejectsMalformedRequests() throws Exception {
        mvc.perform(get("/specialties/not-a-number")).andExpect(status().isBadRequest());
        mvc.perform(post("/specialties").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    private SpecialtyDTO createSpecialty(SpecialtyDTO input) throws Exception {
        String response = mvc.perform(post("/specialties").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(input)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", greaterThan(3)))
                .andExpect(jsonPath("$.name", is(input.getName())))
                .andExpect(jsonPath("$.office", is(input.getOffice())))
                .andExpect(jsonPath("$.openHour", is(input.getOpenHour())))
                .andExpect(jsonPath("$.closeHour", is(input.getCloseHour())))
                .andReturn().getResponse().getContentAsString();
        return json.readValue(response, SpecialtyDTO.class);
    }

    private void expectInvalidSchedule(int open, int close) throws Exception {
        SpecialtyDTO input = SpecialtyTestData.newSpecialty();
        input.setOpenHour(open);
        input.setCloseHour(close);
        mvc.perform(post("/specialties").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
        assertEquals(3, jdbc.queryForObject("SELECT COUNT(*) FROM specialties", Integer.class));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
