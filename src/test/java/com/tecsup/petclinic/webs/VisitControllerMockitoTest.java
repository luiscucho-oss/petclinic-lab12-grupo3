package com.tecsup.petclinic.webs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecsup.petclinic.dtos.VisitDTO;
import com.tecsup.petclinic.exceptions.VisitNotFoundException;
import com.tecsup.petclinic.services.VisitService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class VisitControllerMockitoTest {

    private static final ObjectMapper om = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VisitService visitService;

    private VisitDTO sampleVisit() {
        return VisitDTO.builder()
                .id(1L)
                .visitDate(LocalDate.of(2024, 5, 10))
                .description("Control general")
                .cost(new BigDecimal("80.00"))
                .petId(1)
                .vetId(1)
                .build();
    }

    @Test
    public void testFindAllVisits() throws Exception {
        Mockito.when(visitService.findAll()).thenReturn(List.of(sampleVisit()));

        mockMvc.perform(get("/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Control general")));
    }

    @Test
    public void testFindVisitOK() throws Exception {
        Mockito.when(visitService.findById(1L)).thenReturn(sampleVisit());

        mockMvc.perform(get("/visits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.petId", is(1)));
    }

    @Test
    public void testFindVisitKO() throws Exception {
        Mockito.when(visitService.findById(666L))
                .thenThrow(new VisitNotFoundException("Visit not found"));

        mockMvc.perform(get("/visits/666"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateVisit() throws Exception {
        VisitDTO visit = sampleVisit();
        visit.setId(null);

        Mockito.when(visitService.create(any(VisitDTO.class)))
                .thenReturn(sampleVisit());

        mockMvc.perform(post("/visits")
                        .content(om.writeValueAsString(visit))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("Control general")));
    }

    @Test
    public void testDeleteVisit() throws Exception {
        Mockito.doNothing().when(visitService).delete(1L);

        mockMvc.perform(delete("/visits/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testVisitStatistics() throws Exception {
        Mockito.when(visitService.countByPetId(1)).thenReturn(2L);
        Mockito.when(visitService.sumCostByPetId(1))
                .thenReturn(new BigDecimal("150.00"));

        mockMvc.perform(get("/visits/pet/1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        mockMvc.perform(get("/visits/pet/1/total-cost"))
                .andExpect(status().isOk())
                .andExpect(content().string("150.00"));
    }
}
