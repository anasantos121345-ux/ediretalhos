package br.com.edhi.painel.config;

import br.com.edhi.painel.exception.RegraNegocioException;
import br.com.edhi.painel.model.Usuario;
import br.com.edhi.painel.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Confere o token antes de cada chamada da API.
 * <ul>
 *   <li>{@code /api/auth/cadastro} e {@code /api/auth/login}: abertos.</li>
 *   <li>Resto de {@code /api/auth/**}: qualquer usuário logado.</li>
 *   <li>Resto de {@code /api/**} (vendas, pedidos, feiras, backup): só o admin.</li>
 * </ul>
 */
@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    /** Nome do atributo da requisição onde fica o usuário logado. */
    public static final String USUARIO = "usuarioLogado";

    private final AuthService auth;

    public AutenticacaoInterceptor(AuthService auth) {
        this.auth = auth;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            return true; // pré-checagem do CORS
        }
        String caminho = req.getRequestURI().substring(req.getContextPath().length());
        if (caminho.equals("/api/auth/login") || caminho.equals("/api/auth/cadastro")) {
            return true;
        }
        Usuario u = auth.usuarioDoToken(token(req))
                .orElseThrow(() -> RegraNegocioException.naoAutenticado("Entre na sua conta para continuar."));
        if (!caminho.startsWith("/api/auth/") && !u.isAdmin()) {
            throw RegraNegocioException.proibido("Só a Edilene pode acessar o painel.");
        }
        req.setAttribute(USUARIO, u);
        return true;
    }

    /** Lê o token do cabeçalho {@code Authorization: Bearer <token>}. */
    public static String token(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h != null && h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return h.substring(7).trim();
        }
        return null;
    }
}
