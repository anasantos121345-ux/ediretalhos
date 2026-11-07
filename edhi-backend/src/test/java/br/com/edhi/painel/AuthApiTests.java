package br.com.edhi.painel;

import br.com.edhi.painel.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Cada teste usa um e-mail diferente, porque o limite de tentativas vale para a API inteira. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    private ResultActions enviar(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String tokenDe(ResultActions r) throws Exception {
        String body = r.andReturn().getResponse().getContentAsString();
        return body.replaceAll("(?s).*?\"token\":\"([^\"]+)\".*", "$1");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void cadastraEntraConsultaESai() throws Exception {
        String t1 = tokenDe(enviar("/api/auth/cadastro", """
                {"nome":"Maria Souza","email":"  Maria@Email.com ","senha":"segredo1"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.usuario.email").value("maria@email.com"))
                .andExpect(jsonPath("$.usuario.papel").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.senhaHash").doesNotExist()));

        // A senha fica guardada como hash BCrypt, nunca em texto
        String hash = usuarios.findByEmail("maria@email.com").orElseThrow().getSenhaHash();
        assertTrue(hash.startsWith("$2"));
        assertFalse(hash.contains("segredo1"));

        String t2 = tokenDe(enviar("/api/auth/login", """
                {"email":"MARIA@email.com","senha":"segredo1"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.nome").value("Maria Souza")));
        assertNotEquals(t1, t2);

        mvc.perform(get("/api/auth/eu").header("Authorization", bearer(t2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("maria@email.com"));

        mvc.perform(post("/api/auth/sair").header("Authorization", bearer(t2))).andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/eu").header("Authorization", bearer(t2))).andExpect(status().isUnauthorized());
        // Sair num aparelho não derruba o outro login
        mvc.perform(get("/api/auth/eu").header("Authorization", bearer(t1))).andExpect(status().isOk());
    }

    @Test
    void recusaCadastroInvalidoOuRepetido() throws Exception {
        enviar("/api/auth/cadastro", """
                {"nome":"Ana","email":"ana-sem-arroba","senha":"segredo1"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Digite um e-mail válido."));
        enviar("/api/auth/cadastro", """
                {"nome":"Ana","email":"ana@email.com","senha":"123"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("6 caracteres")));
        enviar("/api/auth/cadastro", """
                {"nome":" ","email":"ana@email.com","senha":"segredo1"}""")
                .andExpect(status().isBadRequest());

        enviar("/api/auth/cadastro", """
                {"nome":"Ana","email":"ana@email.com","senha":"segredo1"}""").andExpect(status().isCreated());
        enviar("/api/auth/cadastro", """
                {"nome":"Outra Ana","email":"ANA@email.com","senha":"outra-senha"}""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(containsString("já tem cadastro")));
        // Ninguém consegue se cadastrar com o e-mail do admin
        enviar("/api/auth/cadastro", """
                {"nome":"Falsa","email":"admin@teste.com","senha":"segredo1"}""").andExpect(status().isConflict());
    }

    @Test
    void senhaErradaEEmailInexistenteDaoAMesmaResposta() throws Exception {
        enviar("/api/auth/cadastro", """
                {"nome":"Bia","email":"bia@email.com","senha":"segredo1"}""").andExpect(status().isCreated());

        enviar("/api/auth/login", """
                {"email":"bia@email.com","senha":"errada"}""")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha incorretos."));
        enviar("/api/auth/login", """
                {"email":"ninguem@email.com","senha":"qualquer"}""")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha incorretos."));
    }

    @Test
    void travaDepoisDeMuitasSenhasErradas() throws Exception {
        enviar("/api/auth/cadastro", """
                {"nome":"Cris","email":"cris@email.com","senha":"segredo1"}""").andExpect(status().isCreated());
        // No teste o limite é 3 (src/test/resources/application.properties)
        for (int i = 0; i < 3; i++) {
            enviar("/api/auth/login", """
                    {"email":"cris@email.com","senha":"errada"}""").andExpect(status().isUnauthorized());
        }
        // Travado mesmo com a senha certa
        enviar("/api/auth/login", """
                {"email":"cris@email.com","senha":"segredo1"}""")
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void painelSoAbreParaOAdmin() throws Exception {
        mvc.perform(get("/api/vendas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/vendas").header("Authorization", "Bearer token-inventado"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/eu")).andExpect(status().isUnauthorized());

        String cliente = tokenDe(enviar("/api/auth/cadastro", """
                {"nome":"Dani","email":"dani@email.com","senha":"segredo1"}"""));
        mvc.perform(get("/api/vendas").header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/dados").header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden());

        String admin = tokenDe(enviar("/api/auth/login", """
                {"email":"admin@teste.com","senha":"senha-admin"}""")
                .andExpect(jsonPath("$.usuario.papel").value("ADMIN")));
        mvc.perform(get("/api/vendas").header("Authorization", bearer(admin))).andExpect(status().isOk());
    }

    @Test
    void preChecagemDoCorsPassaSemToken() throws Exception {
        mvc.perform(options("/api/vendas")
                        .header("Origin", "null")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
}
