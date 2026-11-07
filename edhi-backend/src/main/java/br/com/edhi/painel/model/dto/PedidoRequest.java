package br.com.edhi.painel.model.dto;

import br.com.edhi.painel.model.enums.Canal;
import br.com.edhi.painel.model.enums.Produto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados de um pedido. O estado e a conferência mudam pelos endpoints próprios. */
public record PedidoRequest(String cliente, Produto peca, Integer quantidade,
                            BigDecimal valor, Canal canal, LocalDate prazo) {
}
