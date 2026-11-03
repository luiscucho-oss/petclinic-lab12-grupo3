package com.tecsup.petclinic.webs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecsup.petclinic.dtos.VetDTO;
import com.tecsup.petclinic.exceptions.InvalidVetException;
import com.tecsup.petclinic.exceptions.VetNotFoundException;
import com.tecsup.petclinic.services.VetService;
import com.tecsup.petclinic.support.VetTestData;
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
class VetControllerMockitoTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @MockitoBean
    private VetService service;

    @Test
    void testFindAllVets() throws Exception {
        when(service.findAll()).thenReturn(List.of(VetTestData.existingVet()));
        mvc.perform(get("/vets"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].id", is(1)));
        verify(service).findAll();
    }

    @Test
    void testFindVetOK() throws Exception {
        when(service.findById(1)).thenReturn(VetTestData.existingVet());
        mvc.perform(get("/vets/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("James")))
                .andExpect(jsonPath("$.lastName", is("Carter")))
                .andExpect(jsonPath("$.email", is("james.carter@petclinic.com")))
                .andExpect(jsonPath("$.phone", is("6085551234")))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.specialties").doesNotExist());
        verify(service).findById(1);
    }

    @Test
    void testFindVetKO() throws Exception {
        when(service.findById(666)).thenThrow(new VetNotFoundException("Missing vet"));
        mvc.perform(get("/vets/666")).andExpect(status().isNotFound());
    }

    @Test
    void testCreateVet() throws Exception {
        VetDTO input = VetTestData.newVet();
        VetDTO created = VetTestData.newVet();
        created.setId(100);
        when(service.create(input)).thenReturn(created);
        mvc.perform(post("/vets").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/vets/100"))
                .andExpect(jsonPath("$.id", is(100))).andExpect(jsonPath("$.firstName", is("Lucia")))
                .andExpect(jsonPath("$.lastName", is("Torres")))
                .andExpect(jsonPath("$.email", is("lucia.torres@petclinic.com")))
                .andExpect(jsonPath("$.phone", is("999111222")))
                .andExpect(jsonPath("$.active", is(true)));
        verify(service).create(input);
    }

    @Test
    void testCreateVetDefaultsToActive() throws Exception {
        VetDTO input = VetDTO.builder().firstName("Ana").lastName("Perez").build();
        VetDTO created = VetDTO.builder().id(100).firstName("Ana").lastName("Perez").build();
        when(service.create(input)).thenReturn(created);
        mvc.perform(post("/vets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ana\",\"lastName\":\"Perez\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.active", is(true)));
        verify(service).create(input);
    }

    @Test
    void testCreateVet_BadRequest() throws Exception {
        VetDTO input = VetTestData.newVet();
        input.setLastName(null);
        when(service.create(input)).thenThrow(new InvalidVetException("Last name is required"));
        mvc.perform(post("/vets").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
        verify(service).create(input);
    }

    @Test
    void testUpdateVetUsesThePathId() throws Exception {
        VetDTO input = VetTestData.newVet();
        input.setId(99);
        VetDTO updated = VetTestData.newVet();
        updated.setId(1);
        when(service.update(1, input)).thenReturn(updated);
        mvc.perform(put("/vets/1").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.lastName", is("Torres")));
        verify(service).update(1, input);
        verify(service, never()).update(eq(99), any());
    }

    @Test
    void testUpdateVetKO() throws Exception {
        VetDTO input = VetTestData.newVet();
        when(service.update(666, input)).thenThrow(new VetNotFoundException("Missing vet"));
        mvc.perform(put("/vets/666").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateVet_BadRequest() throws Exception {
        VetDTO input = VetTestData.newVet();
        input.setActive(null);
        when(service.update(1, input)).thenThrow(new InvalidVetException("Active is required"));
        mvc.perform(put("/vets/1").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeactivateVet() throws Exception {
        VetDTO deactivated = VetTestData.existingVet();
        deactivated.setActive(false);
        when(service.deactivate(1)).thenReturn(deactivated);
        mvc.perform(put("/vets/1/deactivate"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.active", is(false)));
        verify(service).deactivate(1);
    }

    @Test
    void testReactivateVet() throws Exception {
        VetDTO reactivated = VetTestData.existingVet();
        reactivated.setId(6);
        when(service.reactivate(6)).thenReturn(reactivated);
        mvc.perform(put("/vets/6/reactivate"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(6)))
                .andExpect(jsonPath("$.active", is(true)));
        verify(service).reactivate(6);
    }

    @Test
    void testChangeActiveVetKO() throws Exception {
        when(service.deactivate(666)).thenThrow(new VetNotFoundException("Missing vet"));
        when(service.reactivate(666)).thenThrow(new VetNotFoundException("Missing vet"));
        mvc.perform(put("/vets/666/deactivate")).andExpect(status().isNotFound());
        mvc.perform(put("/vets/666/reactivate")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @CsvSource({"true,1", "false,6"})
    void testFindVetsByActive(boolean active, int id) throws Exception {
        VetDTO vet = VetTestData.existingVet();
        vet.setId(id);
        vet.setActive(active);
        when(service.findByActive(active)).thenReturn(List.of(vet));
        mvc.perform(get("/vets").param("active", Boolean.toString(active)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(id))).andExpect(jsonPath("$[0].active", is(active)));
        verify(service).findByActive(active);
        verify(service, never()).findAll();
    }

    @Test
    void testDeleteVet() throws Exception {
        mvc.perform(delete("/vets/1")).andExpect(status().isOk());
        verify(service).delete(1);
    }

    @Test
    void testDeleteVetKO() throws Exception {
        doThrow(new VetNotFoundException("Missing vet")).when(service).delete(1000);
        mvc.perform(delete("/vets/1000")).andExpect(status().isNotFound());
        verify(service).delete(1000);
    }

    @Test
    void testMalformedRequestsDoNotReachTheService() throws Exception {
        mvc.perform(get("/vets/not-a-number")).andExpect(status().isBadRequest());
        mvc.perform(get("/vets").param("active", "invalid")).andExpect(status().isBadRequest());
        mvc.perform(post("/vets").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
