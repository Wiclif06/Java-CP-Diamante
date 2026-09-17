package br.edu.campusgigs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:campusgigs;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "campusgigs.jwt.secret=segredo-de-teste-com-mais-de-trinta-e-dois-bytes"
})
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void publicListStartsWithEmptyDatabase() throws Exception {
        mvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void mutationWithoutTokenReturnsCentralized401() throws Exception {
        mvc.perform(post("/api/services")
                        .contentType("application/json")
                        .content("""
                                {"title":"Revisão Java","description":"Aula","category":"EDUCACAO","price":40.00}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
