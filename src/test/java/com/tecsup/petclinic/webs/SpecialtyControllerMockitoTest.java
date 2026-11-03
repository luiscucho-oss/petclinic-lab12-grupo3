package com.tecsup.petclinic.webs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecsup.petclinic.dtos.SpecialtyDTO;
import com.tecsup.petclinic.exceptions.InvalidSpecialtyException;
import com.tecsup.petclinic.exceptions.SpecialtyNotFoundException;
import com.tecsup.petclinic.services.SpecialtyService;
import com.tecsup.petclinic.support.SpecialtyTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SpecialtyControllerMockitoTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @MockitoBean
    private SpecialtyService service;

    @Test
    void testFindAllSpecialties() throws Exception {
        when(service.findAll()).thenReturn(List.of(SpecialtyTestData.existingSpecialty()));
        mvc.perform(get("/specialties"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].id", is(1)));
        verify(service).findAll();
    }

    @Test
    void testFindSpecialtyOK() throws Exception {
        when(service.findById(1)).thenReturn(SpecialtyTestData.existingSpecialty());
        mvc.perform(get("/specialties/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("radiology")))
                .andExpect(jsonPath("$.office", is("Farewell")))
                .andExpect(jsonPath("$.openHour", is(8))).andExpect(jsonPath("$.closeHour", is(18)))
                .andExpect(jsonPath("$.vets").doesNotExist());
        verify(service).findById(1);
    }

    @Test
    void testFindSpecialtyKO() throws Exception {
        when(service.findById(666)).thenThrow(new SpecialtyNotFoundException("Missing specialty"));
        mvc.perform(get("/specialties/666")).andExpect(status().isNotFound());
    }

    @Test
    void testCreateSpecialty() throws Exception {
        SpecialtyDTO input = SpecialtyTestData.newSpecialty();
        SpecialtyDTO created = SpecialtyTestData.newSpecialty();
        created.setId(100);
        when(service.create(input)).thenReturn(created);
        mvc.perform(post("/specialties").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/specialties/100"))
                .andExpect(jsonPath("$.id", is(100))).andExpect(jsonPath("$.name", is("cardiology")))
                .andExpect(jsonPath("$.office", is("Centro")))
                .andExpect(jsonPath("$.openHour", is(8))).andExpect(jsonPath("$.closeHour", is(17)));
        verify(service).create(input);
    }

    @Test
    void testFindByName() throws Exception {
        SpecialtyDTO specialty = SpecialtyTestData.existingSpecialty();
        specialty.setId(2);
        specialty.setName("surgery");
        when(service.findByName("surgery")).thenReturn(List.of(specialty));
        mvc.perform(get("/specialties").param("name", "surgery"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2))).andExpect(jsonPath("$[0].name", is("surgery")));
        verify(service).findByName("surgery");
        verify(service, never()).findAll();
    }

    @Test
    void testFindByOffice() throws Exception {
        when(service.findByOffice("Farewell")).thenReturn(List.of(SpecialtyTestData.existingSpecialty()));
        mvc.perform(get("/specialties").param("office", "Farewell"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("radiology")));
        verify(service).findByOffice("Farewell");
    }

    @Test
    void testFindByNameAndOffice() throws Exception {
        when(service.findByNameAndOffice("radiology", "Farewell"))
                .thenReturn(List.of(SpecialtyTestData.existingSpecialty()));
        when(service.findByNameAndOffice("radiology", "Maryland")).thenReturn(List.of());
        mvc.perform(get("/specialties").param("name", "radiology").param("office", "Farewell"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/specialties").param("name", "radiology").param("office", "Maryland"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(service).findByNameAndOffice("radiology", "Farewell");
        verify(service).findByNameAndOffice("radiology", "Maryland");
        verify(service, never()).findByName(anyString());
    }

    @Test
    void testFindByNameWithoutMatches() throws Exception {
        when(service.findByName("unknown")).thenReturn(List.of());
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
    void testCreateSpecialty_BadRequest() throws Exception {
        SpecialtyDTO input = SpecialtyTestData.newSpecialty();
        input.setName(null);
        when(service.create(input)).thenThrow(new InvalidSpecialtyException("Name is required"));
        mvc.perform(post("/specialties").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
        verify(service).create(input);
    }

    @Test
    void testUpdateSpecialtyUsesThePathId() throws Exception {
        SpecialtyDTO input = SpecialtyTestData.newSpecialty();
        input.setId(99);
        SpecialtyDTO updated = SpecialtyTestData.newSpecialty();
        updated.setId(1);
        when(service.update(1, input)).thenReturn(updated);
        mvc.perform(put("/specialties/1").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("cardiology"))).andExpect(jsonPath("$.office", is("Centro")))
                .andExpect(jsonPath("$.openHour", is(8))).andExpect(jsonPath("$.closeHour", is(17)));
        verify(service).update(1, input);
        verify(service, never()).update(eq(99), any());
    }

    @Test
    void testUpdateSpecialtyKO() throws Exception {
        SpecialtyDTO input = SpecialtyTestData.newSpecialty();
        when(service.update(666, input)).thenThrow(new SpecialtyNotFoundException("Missing specialty"));
        mvc.perform(put("/specialties/666").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateSpecialty_BadRequest() throws Exception {
        SpecialtyDTO input = SpecialtyTestData.newSpecialty();
        input.setOpenHour(20);
        when(service.update(1, input)).thenThrow(new InvalidSpecialtyException("Invalid schedule"));
        mvc.perform(put("/specialties/1").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteSpecialty() throws Exception {
        mvc.perform(delete("/specialties/1")).andExpect(status().isOk());
        verify(service).delete(1);
    }

    @Test
    void testDeleteSpecialtyKO() throws Exception {
        doThrow(new SpecialtyNotFoundException("Missing specialty")).when(service).delete(1000);
        mvc.perform(delete("/specialties/1000")).andExpect(status().isNotFound());
        verify(service).delete(1000);
    }

    @Test
    void testMalformedRequestsDoNotReachTheService() throws Exception {
        mvc.perform(get("/specialties/not-a-number")).andExpect(status().isBadRequest());
        mvc.perform(post("/specialties").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private void expectInvalidSchedule(int open, int close) throws Exception {
        SpecialtyDTO input = SpecialtyTestData.newSpecialty();
        input.setOpenHour(open);
        input.setCloseHour(close);
        when(service.create(input)).thenThrow(new InvalidSpecialtyException("Invalid schedule"));
        mvc.perform(post("/specialties").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
        verify(service).create(input);
    }
}
