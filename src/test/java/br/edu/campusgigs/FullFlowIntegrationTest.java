package br.edu.campusgigs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import br.edu.campusgigs.integration.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:fullflow;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "campusgigs.jwt.secret=segredo-de-teste-com-mais-de-trinta-e-dois-bytes"
})
@AutoConfigureMockMvc
class FullFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean ViaCepClient viaCep;

    @Test
    void completeFlowEnforcesIdentityOwnershipAndStatus() throws Exception {
        when(viaCep.lookup(anyString())).thenReturn(new ViaCepResponse("São Paulo", "SP", false));
        register("Ana", "ana@campus.test");
        register("Bia", "bia@campus.test");
        String tokenA = login("ana@campus.test");
        String tokenB = login("bia@campus.test");

        MvcResult published = mvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Revisão Java","description":"Aula de revisão",
                                 "category":"EDUCACAO","price":40.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.providerId").isNumber())
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andReturn();
        long serviceId = json.readTree(published.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(post("/api/services/{id}/contracts", serviceId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SOLICITADA"));

        mvc.perform(patch("/api/services/{id}/close", serviceId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_ALLOWED"));

        mvc.perform(post("/api/services/{id}/contracts", serviceId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SELF_HIRE"));

        mvc.perform(patch("/api/services/{id}/close", serviceId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADO"));

        mvc.perform(post("/api/services/{id}/contracts", serviceId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SERVICE_NOT_ACTIVE"));
    }

    private void register(String name, String email) throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new Registration(name, email, "SenhaForte123!", "01001000"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.city").value("São Paulo"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    private String login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new Login(email, "SenhaForte123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();
        JsonNode response = json.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }

    private record Registration(String name, String email, String password, String cep) {}
    private record Login(String email, String password) {}
}
