package com.tecsup.petclinic.webs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.tecsup.petclinic.dtos.OwnerDTO;
import com.tecsup.petclinic.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
public class OwnerControllerTest {

    private static final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateFindUpdateDeleteOwner() throws Exception {

        OwnerDTO owner = OwnerDTO.builder()
                .firstName("Kevin")
                .lastName("PruebaXYZ")
                .address("Av. Principal 123")
                .city("Lima")
                .telephone("999999999")
                .build();

        String response = mockMvc.perform(post("/owners")
                        .content(om.writeValueAsString(owner))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lastName", is("PruebaXYZ")))
                .andReturn().getResponse().getContentAsString();

        Number idNumber = JsonPath.parse(response).read("$.id");
        Long id = idNumber.longValue();

        mockMvc.perform(get("/owners/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Kevin")));

        mockMvc.perform(get("/owners/search")
                        .param("lastName", "PruebaXYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastName", is("PruebaXYZ")));

        OwnerDTO updated = OwnerDTO.builder()
                .id(id)
                .firstName("Kevin Actualizado")
                .lastName("PruebaXYZ")
                .address("Nueva dirección")
                .city("Lima")
                .telephone("988888888")
                .build();

        mockMvc.perform(put("/owners/" + id)
                        .content(om.writeValueAsString(updated))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Kevin Actualizado")));

        mockMvc.perform(delete("/owners/" + id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/owners/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSearchOwnerWithoutLastName() throws Exception {
        mockMvc.perform(get("/owners/search").param("lastName", ""))
                .andExpect(status().isBadRequest());
    }
}
