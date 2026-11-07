package br.com.edhi.painel.model.dto;

import br.com.edhi.painel.model.enums.Canal;
import br.com.edhi.painel.model.enums.Produto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados que o painel envia para lançar ou editar uma venda. Sem data, vale a de hoje. */
public record VendaRequest(LocalDate data, String cliente, Produto produto,
                           Integer quantidade, BigDecimal valor, Canal canal) {
}
