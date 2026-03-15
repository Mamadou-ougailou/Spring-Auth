package demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.model.Identity;
import demo.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @Test
    void register_shouldReturnCreatedAndUserId() throws Exception {
        Identity identity = new Identity("john@example.com", "John");
        identity.setId(42L);

        when(registrationService.register(eq("John"), eq("john@example.com"), eq("secret")))
                .thenReturn(identity);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterPayload("john@example.com", "John", "secret"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered. Check your e-mail to verify your account."))
                .andExpect(jsonPath("$.userId").value(42));

        verify(registrationService).register("John", "john@example.com", "secret");
    }

    @Test
    void verify_shouldReturnOk() throws Exception {
        doNothing().when(registrationService).verify(eq("token-id"), eq("hash-value"));

        mockMvc.perform(get("/verify")
                        .param("tokenId", "token-id")
                        .param("t", "hash-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("E-mail verified successfully!"));

        verify(registrationService).verify("token-id", "hash-value");
    }

    private record RegisterPayload(String email, String name, String password) {
    }
}
