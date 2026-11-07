package br.com.edhi.painel.controller;

import br.com.edhi.painel.config.AutenticacaoInterceptor;
import br.com.edhi.painel.model.Usuario;
import br.com.edhi.painel.model.dto.CadastroRequest;
import br.com.edhi.painel.model.dto.LoginRequest;
import br.com.edhi.painel.model.dto.LoginResponse;
import br.com.edhi.painel.model.dto.UsuarioDTO;
import br.com.edhi.painel.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse cadastrar(@RequestBody CadastroRequest req) {
        return service.cadastrar(req);
    }

    @PostMapping("/login")
    public LoginResponse entrar(@RequestBody LoginRequest req) {
        return service.entrar(req);
    }

    /** Quem está logado com este token. */
    @GetMapping("/eu")
    public UsuarioDTO eu(@RequestAttribute(AutenticacaoInterceptor.USUARIO) Usuario usuario) {
        return UsuarioDTO.de(usuario);
    }

    @PostMapping("/sair")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(HttpServletRequest req) {
        service.sair(AutenticacaoInterceptor.token(req));
    }
}
