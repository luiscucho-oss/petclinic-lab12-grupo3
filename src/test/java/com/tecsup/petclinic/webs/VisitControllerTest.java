package com.tecsup.petclinic.webs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.tecsup.petclinic.dtos.VisitDTO;
import com.tecsup.petclinic.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
public class VisitControllerTest {

    private static final ObjectMapper om = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateFindUpdateDeleteVisit() throws Exception {

        VisitDTO visit = VisitDTO.builder()
                .visitDate(LocalDate.of(2024, 5, 10))
                .description("Control general")
                .cost(new BigDecimal("80.00"))
                .petId(1)
                .vetId(1)
                .build();

        String response = mockMvc.perform(post("/visits")
                        .content(om.writeValueAsString(visit))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("Control general")))
                .andReturn().getResponse().getContentAsString();

        Number idNumber = JsonPath.parse(response).read("$.id");
        Long id = idNumber.longValue();

        mockMvc.perform(get("/visits/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.petId", is(1)));

        VisitDTO updated = VisitDTO.builder()
                .id(id)
                .visitDate(LocalDate.of(2024, 5, 10))
                .description("Control actualizado")
                .cost(new BigDecimal("100.00"))
                .petId(1)
                .vetId(1)
                .build();

        mockMvc.perform(put("/visits/" + id)
                        .content(om.writeValueAsString(updated))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Control actualizado")));

        mockMvc.perform(get("/visits/pet/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/visits/vet/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/visits/date-range")
                        .param("startDate", "2024-05-01")
                        .param("endDate", "2024-05-31"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/visits/vet/1/date-range")
                        .param("startDate", "2024-05-01")
                        .param("endDate", "2024-05-31"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/visits/pet/1/count"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/visits/pet/1/total-cost"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/visits/" + id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/visits/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testInvalidDateRange() throws Exception {
        mockMvc.perform(get("/visits/date-range")
                        .param("startDate", "2024-12-31")
                        .param("endDate", "2024-01-01"))
                .andExpect(status().isBadRequest());
    }
}
