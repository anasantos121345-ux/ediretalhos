package br.com.edhi.painel.service;

import br.com.edhi.painel.exception.RegraNegocioException;

import java.math.BigDecimal;

/** Checagens repetidas nos services. Todas devolvem erro 400 com a mensagem em português. */
final class Validacao {

    private Validacao() {
    }

    static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw RegraNegocioException.invalido("Preencha o campo " + campo + ".");
        }
        return valor.trim();
    }

    static <T> T obrigatorio(T valor, String campo) {
        if (valor == null) {
            throw RegraNegocioException.invalido("Preencha o campo " + campo + ".");
        }
        return valor;
    }

    static BigDecimal naoNegativo(BigDecimal valor, String campo) {
        obrigatorio(valor, campo);
        if (valor.signum() < 0) {
            throw RegraNegocioException.invalido("O campo " + campo + " não pode ser negativo.");
        }
        return valor;
    }

    static Integer quantidade(Integer qtd) {
        obrigatorio(qtd, "quantidade");
        if (qtd < 1) {
            throw RegraNegocioException.invalido("A quantidade precisa ser pelo menos 1.");
        }
        return qtd;
    }
}
