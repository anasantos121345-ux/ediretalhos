package br.com.edhi.painel.model.dto;

import br.com.edhi.painel.model.Usuario;
import br.com.edhi.painel.model.enums.Papel;

/** O que a API mostra de um usuário. Nunca inclui a senha. */
public record UsuarioDTO(Long id, String nome, String email, Papel papel) {

    public static UsuarioDTO de(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getNome(), u.getEmail(), u.getPapel());
    }
}
