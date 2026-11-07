package br.com.edhi.painel;

import br.com.edhi.painel.service.BackupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PainelApiTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    BackupService backupService;

    @BeforeEach
    void limpar() {
        backupService.apagarTudo();
    }

    private ResultActions enviar(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long idDe(ResultActions r) throws Exception {
        String body = r.andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll("(?s).*?\"id\":(\\d+).*", "$1"));
    }

    @Test
    void lancaListaEApagaVendaComRotulosDoPainel() throws Exception {
        long id = idDe(enviar("/api/vendas", """
                {"data":"2026-11-07","cliente":"Faculdade","produto":"Ecobag","quantidade":140,
                 "valor":3000,"canal":"Empresa e brinde"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value("2026-11-07"))
                .andExpect(jsonPath("$.produto").value("Ecobag"))
                .andExpect(jsonPath("$.canal").value("Empresa e brinde")));
        enviar("/api/vendas", """
                {"cliente":"Ana","produto":"PANO_DE_COPA","quantidade":2,"valor":1000,"canal":"whatsapp e instagram"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.produto").value("Pano de copa"));

        mvc.perform(get("/api/vendas")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(delete("/api/vendas/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/vendas")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void recusaDadosInvalidos() throws Exception {
        enviar("/api/vendas", """
                {"cliente":"X","produto":"Ecobag","quantidade":1,"valor":-5,"canal":"Feira"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("negativo")));
        enviar("/api/vendas", """
                {"cliente":"X","produto":"Sofá","quantidade":1,"valor":5,"canal":"Feira"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
        enviar("/api/pedidos", """
                {"cliente":" ","peca":"Tapete","quantidade":1,"valor":5,"canal":"Feira"}""")
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/vendas/999")).andExpect(status().isNotFound());
    }

    @Test
    void pedidoSoEntregaConferidoESoViraVendaUmaVez() throws Exception {
        long id = idDe(enviar("/api/pedidos", """
                {"cliente":"Igreja","peca":"Tapete","quantidade":2,"valor":520,"canal":"Feira",
                 "prazo":"%s"}""".formatted(LocalDate.now().plusDays(3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Novo")));

        mvc.perform(patch("/api/pedidos/" + id + "/avancar")).andExpect(jsonPath("$.status").value("Em produção"));
        mvc.perform(patch("/api/pedidos/" + id + "/avancar")).andExpect(jsonPath("$.status").value("Pronto para conferir"));
        mvc.perform(patch("/api/pedidos/" + id + "/avancar")).andExpect(status().isConflict());

        for (String item : new String[]{"medida", "estampa", "cheiro"}) {
            mvc.perform(patch("/api/pedidos/" + id + "/conferencia/" + item)).andExpect(status().isOk());
        }
        mvc.perform(post("/api/pedidos/" + id + "/lancar-venda")).andExpect(status().isConflict());
        mvc.perform(patch("/api/pedidos/" + id + "/avancar")).andExpect(jsonPath("$.status").value("Entregue"));

        mvc.perform(post("/api/pedidos/" + id + "/lancar-venda"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(520));
        mvc.perform(post("/api/pedidos/" + id + "/lancar-venda")).andExpect(status().isConflict());
        mvc.perform(get("/api/pedidos")).andExpect(jsonPath("$[0].lancado").value(true));

        // Apagar o pedido mantém a venda
        mvc.perform(delete("/api/pedidos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/vendas")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void feirasOrdenadasPorRetorno() throws Exception {
        enviar("/api/feiras", """
                {"nome":"Feira Preta","custo":1800,"vendas":5200}""").andExpect(status().isCreated());
        long id = idDe(enviar("/api/feiras", """
                {"nome":"Feira do Rosário","custo":400,"vendas":2100}""").andExpect(status().isCreated()));

        mvc.perform(get("/api/feiras"))
                .andExpect(jsonPath("$[0].nome").value("Feira do Rosário"))
                .andExpect(jsonPath("$[0].retorno").value(5.25));
        mvc.perform(delete("/api/feiras/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/feiras")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void importaEExportaBackupDoNavegador() throws Exception {
        enviar("/api/backup", """
                {"sales":[{"id":"abc","date":"2026-01-10","cliente":"Ana","produto":"Colcha","qtd":1,"valor":450.5,"canal":"Feira"}],
                 "orders":[{"id":"x1","cliente":"Bia","peca":"Ecobag","qtd":10,"valor":200,"canal":"WhatsApp e Instagram",
                            "prazo":"","status":2,"checks":[true,false,true],"launched":false}],
                 "fairs":[{"id":"f","nome":"Feira X","data":"","custo":0,"vendas":300}]}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendas").value(1));

        mvc.perform(get("/api/backup"))
                .andExpect(jsonPath("$.sales[0].produto").value("Colcha"))
                .andExpect(jsonPath("$.orders[0].status").value(2))
                .andExpect(jsonPath("$.orders[0].checks[2]").value(true))
                .andExpect(jsonPath("$.orders[0].prazo").value(""))
                .andExpect(jsonPath("$.fairs[0].nome").value("Feira X"));

        mvc.perform(delete("/api/dados")).andExpect(status().isNoContent());
        mvc.perform(get("/api/vendas")).andExpect(jsonPath("$", hasSize(0)));
    }
}
