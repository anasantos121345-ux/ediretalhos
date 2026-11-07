package br.com.edhi.painel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trava o login de um e-mail depois de muitas senhas erradas seguidas,
 * para ninguém ficar chutando senhas. Fica em memória: reiniciar a API zera a contagem.
 */
@Component
class LimiteTentativas {

    private final int maximo;
    private final Duration janela;
    private final Map<String, Deque<Instant>> falhas = new ConcurrentHashMap<>();

    LimiteTentativas(@Value("${edhi.login.max-tentativas:5}") int maximo,
                     @Value("${edhi.login.janela-minutos:15}") long janelaMinutos) {
        this.maximo = maximo;
        this.janela = Duration.ofMinutes(janelaMinutos);
    }

    boolean bloqueado(String email) {
        Deque<Instant> lista = falhas.get(email);
        if (lista == null) {
            return false;
        }
        synchronized (lista) {
            limparAntigas(lista);
            return lista.size() >= maximo;
        }
    }

    void registrarFalha(String email) {
        Deque<Instant> lista = falhas.computeIfAbsent(email, k -> new ArrayDeque<>());
        synchronized (lista) {
            limparAntigas(lista);
            lista.addLast(Instant.now());
        }
    }

    void limpar(String email) {
        falhas.remove(email);
    }

    private void limparAntigas(Deque<Instant> lista) {
        Instant limite = Instant.now().minus(janela);
        while (!lista.isEmpty() && lista.peekFirst().isBefore(limite)) {
            lista.removeFirst();
        }
    }
}
