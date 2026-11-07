package br.com.edhi.painel.config;

import br.com.edhi.painel.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Ao subir a API, cria a conta de admin da Edilene se ela ainda não existir. */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final AuthService auth;
    private final String nome;
    private final String email;
    private final String senha;
    private final boolean producao;

    public AdminInicial(AuthService auth,
                        @Value("${edhi.admin.nome}") String nome,
                        @Value("${edhi.admin.email}") String email,
                        @Value("${edhi.admin.senha}") String senha,
                        @Value("${edhi.producao:false}") boolean producao) {
        this.auth = auth;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.producao = producao;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (producao && (senha == null || senha.isBlank() || senha.equals("1234567"))) {
            throw new IllegalStateException("No ar, a senha da Edilene não pode ser a padrão. "
                    + "Defina a variável de ambiente EDHI_ADMIN_SENHA no Render (Environment).");
        }
        if (auth.garantirAdmin(nome, email, senha)) {
            log.info("Conta de admin criada para {}", email);
        }
    }
}
