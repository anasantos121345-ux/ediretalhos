package br.com.edhi.painel.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Um login ativo. O navegador guarda o token; aqui fica só o hash SHA-256 dele,
 * para que quem ler o banco não consiga se passar pelo usuário.
 */
@Entity
@Table(name = "tb_sessao")
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    public Sessao() {
    }

    public Sessao(String tokenHash, Usuario usuario, LocalDateTime expiraEm) {
        this.tokenHash = tokenHash;
        this.usuario = usuario;
        this.expiraEm = expiraEm;
    }

    public boolean expirada() {
        return LocalDateTime.now().isAfter(expiraEm);
    }

    public Long getId() { return id; }
    public String getTokenHash() { return tokenHash; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
}
