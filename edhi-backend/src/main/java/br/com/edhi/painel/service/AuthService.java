package br.com.edhi.painel.service;

import br.com.edhi.painel.exception.RegraNegocioException;
import br.com.edhi.painel.model.Sessao;
import br.com.edhi.painel.model.Usuario;
import br.com.edhi.painel.model.dto.CadastroRequest;
import br.com.edhi.painel.model.dto.LoginRequest;
import br.com.edhi.painel.model.dto.LoginResponse;
import br.com.edhi.painel.model.dto.UsuarioDTO;
import br.com.edhi.painel.model.enums.Papel;
import br.com.edhi.painel.repository.SessaoRepository;
import br.com.edhi.painel.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final SessaoRepository sessoes;
    private final LimiteTentativas limite;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final long horasSessao;
    /** Usado quando o e-mail não existe, para o login demorar o mesmo tanto e não revelar quem tem conta. */
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarios, SessaoRepository sessoes, LimiteTentativas limite,
                       @Value("${edhi.sessao.horas:168}") long horasSessao) {
        this.usuarios = usuarios;
        this.sessoes = sessoes;
        this.limite = limite;
        this.horasSessao = horasSessao;
        this.hashFalso = encoder.encode("senha-que-ninguem-usa");
    }

    /** Cria a conta de cliente e já deixa a pessoa logada. */
    @Transactional
    public LoginResponse cadastrar(CadastroRequest req) {
        Validacao.obrigatorio(req, "cadastro");
        String nome = Validacao.texto(req.nome(), "nome");
        if (nome.length() < 2) {
            throw RegraNegocioException.invalido("Digite seu nome.");
        }
        String email = email(req.email());
        String senha = senhaNova(req.senha());
        if (usuarios.existsByEmail(email)) {
            throw RegraNegocioException.conflito("Este e-mail já tem cadastro. Toque em Login.");
        }
        Usuario u = usuarios.save(new Usuario(nome, email, encoder.encode(senha), Papel.CLIENTE));
        return abrirSessao(u);
    }

    @Transactional
    public LoginResponse entrar(LoginRequest req) {
        Validacao.obrigatorio(req, "login");
        String email = email(req.email());
        String senha = Validacao.texto(req.senha(), "senha");
        if (limite.bloqueado(email)) {
            throw RegraNegocioException.muitasTentativas(
                    "Muitas tentativas com senha errada. Espere alguns minutos e tente de novo.");
        }
        Optional<Usuario> achado = usuarios.findByEmail(email);
        boolean confere = encoder.matches(senha, achado.map(Usuario::getSenhaHash).orElse(hashFalso));
        if (achado.isEmpty() || !confere) {
            limite.registrarFalha(email);
            throw RegraNegocioException.naoAutenticado("E-mail ou senha incorretos.");
        }
        limite.limpar(email);
        sessoes.apagarExpiradas(LocalDateTime.now());
        return abrirSessao(achado.get());
    }

    /** Usuário dono do token, ou vazio se o token não existe ou já venceu. */
    @Transactional
    public Optional<Usuario> usuarioDoToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Optional<Sessao> sessao = sessoes.findByTokenHash(sha256(token));
        if (sessao.isEmpty()) {
            return Optional.empty();
        }
        if (sessao.get().expirada()) {
            sessoes.delete(sessao.get());
            return Optional.empty();
        }
        return Optional.of(sessao.get().getUsuario());
    }

    @Transactional
    public void sair(String token) {
        if (token != null && !token.isBlank()) {
            sessoes.deleteByTokenHash(sha256(token));
        }
    }

    /** Cria a conta da Edilene na primeira vez que a API sobe. Não mexe se ela já existir. */
    @Transactional
    public boolean garantirAdmin(String nome, String email, String senha) {
        String e = email(email);
        if (usuarios.existsByEmail(e)) {
            return false;
        }
        usuarios.save(new Usuario(Validacao.texto(nome, "nome do admin"), e,
                encoder.encode(senhaNova(senha)), Papel.ADMIN));
        return true;
    }

    private LoginResponse abrirSessao(Usuario u) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime expira = LocalDateTime.now().plusHours(horasSessao);
        sessoes.save(new Sessao(sha256(token), u, expira));
        return new LoginResponse(token, expira, UsuarioDTO.de(u));
    }

    private static String email(String valor) {
        String e = Validacao.texto(valor, "e-mail").toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(e).matches()) {
            throw RegraNegocioException.invalido("Digite um e-mail válido.");
        }
        return e;
    }

    private static String senhaNova(String senha) {
        if (senha == null || senha.length() < 6) {
            throw RegraNegocioException.invalido("A senha precisa ter pelo menos 6 caracteres.");
        }
        // O BCrypt só usa os primeiros 72 bytes da senha
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw RegraNegocioException.invalido("A senha pode ter no máximo 72 caracteres.");
        }
        return senha;
    }

    private static String sha256(String texto) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
