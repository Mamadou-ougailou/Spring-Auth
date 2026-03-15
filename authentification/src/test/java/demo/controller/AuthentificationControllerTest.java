package demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.model.Identity;
import demo.model.Token;
import demo.service.AuthentificationService;
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

@WebMvcTest(controllers = AuthentificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthentificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthentificationService authService;

    @Test
    void emailLogin_shouldReturnTokenAndExpiration() throws Exception {
        Token token = new Token();
        token.setToken("abc-token");
        token.setExpirationTime(123456789L);

        when(authService.emailLogin(eq("john@example.com"), eq("secret"))).thenReturn(token);

        mockMvc.perform(post("/auth/email/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload("john@example.com", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("abc-token"))
                .andExpect(jsonPath("$.expirationTime").value(123456789L));

        verify(authService).emailLogin("john@example.com", "secret");
    }

    @Test
    void logout_shouldReturnOk() throws Exception {
        doNothing().when(authService).logout(eq("token-123"));

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "token-123"))
                .andExpect(status().isOk());

        verify(authService).logout("token-123");
    }

    @Test
    void changePassword_shouldReturnOk() throws Exception {
        doNothing().when(authService).changePassword(eq("john@example.com"), eq("old-pass"), eq("new-pass"));

        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "token-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordPayload("john@example.com", "old-pass", "new-pass"))))
                .andExpect(status().isOk());

        verify(authService).changePassword("john@example.com", "old-pass", "new-pass");
    }

    @Test
    void getUserInfo_shouldReturnEmailAndName() throws Exception {
        Identity identity = new Identity("john@example.com", "John");
        when(authService.getUserInfo(eq("token-123"))).thenReturn(identity);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.name").value("John"));

        verify(authService).getUserInfo("token-123");
    }

    @Test
    void validate_shouldReturnOk() throws Exception {
        doNothing().when(authService).validateTokenForTarget(eq("token-123"), eq("A"));

        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "token-123")
                        .header("X-Target-Service", "A"))
                .andExpect(status().isOk());

        verify(authService).validateTokenForTarget("token-123", "A");
    }

    private record LoginPayload(String email, String password) {
    }

    private record ChangePasswordPayload(String email, String oldPassword, String newPassword) {
    }
}
