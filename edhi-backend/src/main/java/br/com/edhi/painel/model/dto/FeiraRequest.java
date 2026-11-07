package br.com.edhi.painel.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeiraRequest(String nome, LocalDate data, BigDecimal custo, BigDecimal vendas) {
}
