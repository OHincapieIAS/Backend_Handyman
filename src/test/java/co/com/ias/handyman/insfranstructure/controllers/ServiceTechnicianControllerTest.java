package co.com.ias.handyman.insfranstructure.controllers;

import co.com.ias.handyman.infranstructure.controllers.ServiceTechnicianController;
import co.com.ias.handyman.infranstructure.controllers.TechnicianController;
import co.com.ias.handyman.infranstructure.models.CalculatorHoursDTO;
import co.com.ias.handyman.infranstructure.models.ServiceTechnicianDTO;
import co.com.ias.handyman.serviceTechnician.application.ports.input.CalculatorHoursWorkUseCase;
import co.com.ias.handyman.serviceTechnician.application.ports.input.CreateServiceTechnicianUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@WebMvcTest(ServiceTechnicianController.class)
public class ServiceTechnicianControllerTest {
    @MockBean
    private CreateServiceTechnicianUseCase createServiceTechnicianUseCase;

    @MockBean
    private CalculatorHoursWorkUseCase calculatorHoursWorkUseCase;

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("Create Service Technician successfully")
    public void CreateTechnicianController(){


    }

    @Test
    @DisplayName("Calculate a ServiceTechnician return a List of CalculatorHourDTO")
    void successCalculate() throws Exception {
        ServiceTechnicianDTO dto = new ServiceTechnicianDTO();
        dto.setIdService(null);
        dto.setIdTechnician(4L);
        dto.setStartDate(LocalDateTime.now().minusDays(7));
        dto.setFinalDate(LocalDateTime.now());
        Optional<List<CalculatorHoursDTO>> calculatorHoursDTOS = Optional.empty();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        when(calculatorHoursWorkUseCase.execute(any(ServiceTechnicianDTO.class))).thenReturn(calculatorHoursDTOS);

        mockMvc.perform(MockMvcRequestBuilders.post("/service-technician/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
