package br.com.edhi.painel.model;

import br.com.edhi.painel.model.enums.Papel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    /** Sempre em minúsculas, sem espaços nas pontas. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Hash BCrypt da senha. A senha em si nunca é guardada. */
    @Column(nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    public Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, Papel papel) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.papel = papel;
        this.criadoEm = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return papel == Papel.ADMIN;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public Papel getPapel() { return papel; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
