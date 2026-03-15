package demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.dto.AdminIdentityResponse;
import demo.model.Authority;
import demo.model.Credential;
import demo.model.Token;
import demo.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @Test
    void getAllIdentities_shouldReturnOk() throws Exception {
        when(adminService.getAllUsers(eq("admin-token")))
                .thenReturn(List.of(new AdminIdentityResponse(1L, "admin@admin.com", "Admin", true, List.of("admin"))));

        mockMvc.perform(get("/admin/identities").header("Authorization", "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@admin.com"));

        verify(adminService).getAllUsers("admin-token");
    }

    @Test
    void getConnectedIdentities_shouldReturnOk() throws Exception {
        when(adminService.getAllConnectedUsers(eq("admin-token")))
                .thenReturn(Set.of(new AdminIdentityResponse(1L, "admin@admin.com", "Admin", true, List.of("admin"))));

        mockMvc.perform(get("/admin/identities/connected").header("Authorization", "admin-token"))
                .andExpect(status().isOk());

        verify(adminService).getAllConnectedUsers("admin-token");
    }

    @Test
    void deleteIdentity_shouldReturnOk() throws Exception {
        doNothing().when(adminService).deleteUser(eq("admin-token"), eq("user@example.com"));

        mockMvc.perform(delete("/admin/identities/{email}", "user@example.com")
                        .header("Authorization", "admin-token"))
                .andExpect(status().isOk());

        verify(adminService).deleteUser("admin-token", "user@example.com");
    }

    @Test
    void getAllCredentials_shouldReturnOk() throws Exception {
        Credential credential = new Credential("admin");
        credential.setId(1L);
        when(adminService.getAllCredentials(eq("admin-token"))).thenReturn(List.of(credential));

        mockMvc.perform(get("/admin/credentials").header("Authorization", "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("admin"));

        verify(adminService).getAllCredentials("admin-token");
    }

    @Test
    void addCredential_shouldReturnOk() throws Exception {
        doNothing().when(adminService).addCredential(eq("admin-token"), eq("role-x"));

        mockMvc.perform(post("/admin/credentials")
                        .header("Authorization", "admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddCredentialPayload("role-x"))))
                .andExpect(status().isOk());

        verify(adminService).addCredential("admin-token", "role-x");
    }

    @Test
    void addCredentialToIdentity_shouldReturnOk() throws Exception {
        doNothing().when(adminService).addRoleToUser(eq("admin-token"), eq("user@example.com"), eq("a"));

        mockMvc.perform(post("/admin/identities/{email}/credentials", "user@example.com")
                        .header("Authorization", "admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddRolePayload("a"))))
                .andExpect(status().isOk());

        verify(adminService).addRoleToUser("admin-token", "user@example.com", "a");
    }

    @Test
    void removeCredentialFromIdentity_shouldReturnOk() throws Exception {
        doNothing().when(adminService).removeRoleFromUser(eq("admin-token"), eq("user@example.com"), eq("a"));

        mockMvc.perform(delete("/admin/identities/{email}/credentials/{roleName}", "user@example.com", "a")
                        .header("Authorization", "admin-token"))
                .andExpect(status().isOk());

        verify(adminService).removeRoleFromUser("admin-token", "user@example.com", "a");
    }

    @Test
    void getAllAuthorities_shouldReturnOk() throws Exception {
        Authority authority = new Authority(Authority.Provider.EMAIL, "secret");
        authority.setId(10L);
        when(adminService.getAllAuthorities(eq("admin-token"))).thenReturn(List.of(authority));

        mockMvc.perform(get("/admin/authorities").header("Authorization", "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].provider").value("EMAIL"));

        verify(adminService).getAllAuthorities("admin-token");
    }

    @Test
    void addAuthorityToIdentity_shouldReturnOk() throws Exception {
        doNothing().when(adminService).addAuthorityToUser(eq("admin-token"), eq("user@example.com"), eq(Authority.Provider.EMAIL), eq("secret"));

        mockMvc.perform(post("/admin/identities/{email}/authorities", "user@example.com")
                        .header("Authorization", "admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddAuthorityPayload("EMAIL", "secret"))))
                .andExpect(status().isOk());

        verify(adminService).addAuthorityToUser("admin-token", "user@example.com", Authority.Provider.EMAIL, "secret");
    }

    @Test
    void removeAuthorityFromIdentity_shouldReturnOk() throws Exception {
        doNothing().when(adminService).removeAuthorityFromUser(eq("admin-token"), eq("user@example.com"), eq(Authority.Provider.EMAIL));

        mockMvc.perform(delete("/admin/identities/{email}/authorities/{provider}", "user@example.com", "EMAIL")
                        .header("Authorization", "admin-token"))
                .andExpect(status().isOk());

        verify(adminService).removeAuthorityFromUser("admin-token", "user@example.com", Authority.Provider.EMAIL);
    }

    @Test
    void getAllTokens_shouldReturnOk() throws Exception {
        Token token = new Token();
        token.setId(1L);
        token.setToken("token-1");
        token.setExpirationTime(System.currentTimeMillis() + 10000);
        when(adminService.getAllTokens(eq("admin-token"))).thenReturn(List.of(token));

        mockMvc.perform(get("/admin/tokens").header("Authorization", "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].token").value("token-1"));

        verify(adminService).getAllTokens("admin-token");
    }

    @Test
    void deleteToken_shouldReturnOk() throws Exception {
        doNothing().when(adminService).deleteToken(eq("admin-token"), eq("token-1"));

        mockMvc.perform(delete("/admin/tokens/{token}", "token-1")
                        .header("Authorization", "admin-token"))
                .andExpect(status().isOk());

        verify(adminService).deleteToken("admin-token", "token-1");
    }

    private record AddCredentialPayload(String name) {
    }

    private record AddRolePayload(String roleName) {
    }

    private record AddAuthorityPayload(String provider, String secret) {
    }
}
