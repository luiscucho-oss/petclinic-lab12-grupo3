package com.tecsup.petclinic.webs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecsup.petclinic.dtos.OwnerDTO;
import com.tecsup.petclinic.exceptions.OwnerNotFoundException;
import com.tecsup.petclinic.services.OwnerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OwnerControllerMockitoTest {

    private static final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerService ownerService;

    @Test
    public void testFindAllOwners() throws Exception {
        OwnerDTO owner = OwnerDTO.builder()
                .id(1L).firstName("Kevin").lastName("Vargas")
                .address("Lima").city("Lima").telephone("999999999")
                .build();

        Mockito.when(ownerService.findAll()).thenReturn(List.of(owner));

        mockMvc.perform(get("/owners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].lastName", is("Vargas")));
    }

    @Test
    public void testFindOwnerOK() throws Exception {
        OwnerDTO owner = OwnerDTO.builder()
                .id(1L).firstName("Kevin").lastName("Vargas")
                .build();

        Mockito.when(ownerService.findById(1L)).thenReturn(owner);

        mockMvc.perform(get("/owners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Kevin")));
    }

    @Test
    public void testFindOwnerKO() throws Exception {
        Mockito.when(ownerService.findById(666L))
                .thenThrow(new OwnerNotFoundException("Owner not found"));

        mockMvc.perform(get("/owners/666"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateOwner() throws Exception {
        OwnerDTO owner = OwnerDTO.builder()
                .firstName("Kevin").lastName("Vargas")
                .address("Lima").city("Lima").telephone("999999999")
                .build();

        OwnerDTO created = OwnerDTO.builder()
                .id(10L).firstName("Kevin").lastName("Vargas")
                .address("Lima").city("Lima").telephone("999999999")
                .build();

        Mockito.when(ownerService.create(any(OwnerDTO.class)))
                .thenReturn(created);

        mockMvc.perform(post("/owners")
                        .content(om.writeValueAsString(owner))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.lastName", is("Vargas")));
    }

    @Test
    public void testDeleteOwner() throws Exception {
        Mockito.doNothing().when(ownerService).delete(1L);

        mockMvc.perform(delete("/owners/1"))
                .andExpect(status().isOk());
    }
}
