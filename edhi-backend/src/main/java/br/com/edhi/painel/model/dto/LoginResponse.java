package br.com.edhi.painel.model.dto;

import java.time.LocalDateTime;

/** Resposta do login e do cadastro. O token vai no cabeçalho {@code Authorization: Bearer <token>}. */
public record LoginResponse(String token, LocalDateTime expiraEm, UsuarioDTO usuario) {
}
